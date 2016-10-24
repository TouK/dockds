package pl.touk.dockds;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.NetworkSettings;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class DockerizedDataSource extends DelegatingDataSource {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DockerizedDataSource.class);

    public static final String CONTAINER_NAME_PREFIX = "dockds-";

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
                        .url(getDatabaseUrl())
                        .build()
        );

    }

    @PreDestroy
    public void destroyContainer() throws Exception {
        docker.stopContainer(containerInfo.id(), 30);
        docker.removeContainer(containerInfo.id(), DockerClient.RemoveContainerParam.removeVolumes());
    }

    protected void checkStaleContainers() throws DockerException, InterruptedException {
        Long staleContainersCount = docker.listContainers(
                DockerClient.ListContainersParam.allContainers()
        ).stream()
                .flatMap(c -> c.names().stream())
                .filter(name -> name.startsWith("/" + CONTAINER_NAME_PREFIX))
                .peek(log::debug)
                .collect(Collectors.counting());
        ;
        log.warn("Found {} probably stale containers. Consider removing them.", staleContainersCount);
    }

    protected void createContainer() throws DockerException, InterruptedException, IOException {

        log.info("Creating database container");

        final HostConfig hostConfig = HostConfig.builder().publishAllPorts(true).build();

        ContainerConfig containerConfig = ContainerConfig.builder().env(type.getEnv()).image(type.getImage()).hostConfig(hostConfig).build();
        ContainerCreation containerCreation = docker.createContainer(containerConfig, CONTAINER_NAME_PREFIX + UUID.randomUUID());
        if (!CollectionUtils.isEmpty(containerCreation.getWarnings())) {
            containerCreation.getWarnings().forEach(DockerizedDataSource.log::warn);
        }

        docker.startContainer(containerCreation.id());
        this.containerInfo = docker.inspectContainer(containerCreation.id());
    }

    protected void waitForDatabaseStart(String url, String username, String password) throws InterruptedException, SQLException {
        for (int i = 0; i < 10; i++) {
            try {
                if (DriverManager.getConnection(url, username, password).isValid(10)) {
                    log.info("Database container seems to have started");
                    return;
                }
            } catch (SQLException e) {
                Thread.sleep(3000);
            }
            log.info("Waiting for the container to start up...");
        }
        log.error("Database container has probably not started");

    }

    public String getDatabaseUrl() {
        return Optional.ofNullable(containerInfo)
                .map(ContainerInfo::networkSettings)
                .map(NetworkSettings::ports)
                .flatMap(ports -> ports.entrySet().stream().map(Map.Entry::getValue).flatMap(List::stream).findFirst())
                .map(a -> type.getUrl().expand(a.hostIp(), a.hostPort()).toUriString())
                .get();
    }

}
