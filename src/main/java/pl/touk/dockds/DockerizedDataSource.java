package pl.touk.dockds;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DockerizedDataSource extends DelegatingDataSource implements Closeable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DockerizedDataSource.class);

    public static final String CONTAINER_NAME_PREFIX = "dockds-";
    private static final int STARTUP_CHECK_INTERVAL = 5000;
    private static final int STARTUP_CHECK_ATTEMPTS = 30;

    protected final DockerizedDatabase type;

    protected final DockerClient docker;

    protected ContainerInfo containerInfo;

    public DockerizedDataSource(DockerizedDatabase type) throws Exception {
        this.type = type;
        this.docker = DefaultDockerClient.fromEnv().build();
    }

    @PostConstruct
    public void startDatabseInContainer() throws Exception {

        checkStaleContainers();
        createContainer();

        waitForDatabaseStart(
                getDatabaseUrl(),
                type.getUsername(),
                type.getPassword()
        );

        setTargetDataSource(
                DataSourceBuilder.create()
                        .username(type.getUsername())
                        .password(type.getPassword())
                        .url(getDatabaseUrl().toString())
                        .build()
        );

    }

    @PreDestroy
    public void destroyContainer() throws Exception {
        log.info("Removing the database container");
        close();
        docker.stopContainer(containerInfo.id(), 30);
        docker.removeContainer(containerInfo.id(), DockerClient.RemoveContainerParam.removeVolumes());
        log.info("The database container has been successfully removed");
    }

    protected void checkStaleContainers() throws DockerException, InterruptedException {
        Long staleContainersCount = docker.listContainers(
                DockerClient.ListContainersParam.allContainers()
        ).stream()
                .flatMap(c -> c.names().stream())
                .filter(name -> name.startsWith("/" + CONTAINER_NAME_PREFIX))
                .peek(log::debug)
                .count();
        if (staleContainersCount > 0) {
            log.warn("Found {} probably stale containers. Consider removing them.", staleContainersCount);
        }
    }

    protected void createContainer() throws DockerException, InterruptedException, IOException {

        log.info("Creating database container");

        final HostConfig hostConfig = HostConfig.builder().publishAllPorts(true).build();

        ContainerConfig containerConfig = ContainerConfig.builder().env(type.getEnv()).image(type.getImage()).hostConfig(hostConfig).build();
        ContainerCreation containerCreation = docker.createContainer(containerConfig, CONTAINER_NAME_PREFIX + UUID.randomUUID());
        Optional.ofNullable(containerCreation.warnings()).ifPresent(it -> it.forEach(DockerizedDataSource.log::warn));

        docker.startContainer(containerCreation.id());
        this.containerInfo = docker.inspectContainer(containerCreation.id());
    }

    protected void waitForDatabaseStart(URI url, String username, String password) throws InterruptedException, SQLException {
        for (int i = 0; i < STARTUP_CHECK_ATTEMPTS; i++) {
            log.info("Waiting for the container to start up on {} ...", url                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             );
            try {
                if (DriverManager.getConnection(url.toString(), username, password).isValid(10)) {
                    log.info("Database container seems to have started");
                    return;
                }
            } catch (SQLException e) {
                Thread.sleep(STARTUP_CHECK_INTERVAL);
            }
        }
        log.error("Database container has probably not started");

    }

    @Override
    public void close() throws IOException {
        DataSource targetDataSource = getTargetDataSource();
        if (targetDataSource instanceof Closeable) {
            ((Closeable) targetDataSource).close();
        }
    }

    public URI getDatabaseUrl() {
        return Optional.ofNullable(containerInfo)
                .map(ContainerInfo::networkSettings)
                .flatMap(this::firstExposedPort)
                .map(port -> type.getUrl(docker.getHost(), port))
                .get();
    }

    private Optional<String> firstExposedPort(NetworkSettings networkSettings) {
        return networkSettings.ports().values().stream().flatMap(List::stream).findFirst().map(PortBinding::hostPort);
    }

}
