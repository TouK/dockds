package pl.touk.dockds;

import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public enum DockerizedDatabase {

    POSTGRES("org.postgresql.Driver", "postgres:latest", UriComponentsBuilder.fromUriString("jdbc:postgresql://{host}:{port}/").build(), "postgres", null),
    MYSQL("com.mysql.jdbc.Driver", "mysql:latest", UriComponentsBuilder.fromUriString("jdbc:mysql://{host}:{port}/dockds?useSSL=false").build(), "root", null, "MYSQL_ALLOW_EMPTY_PASSWORD=yes", "MYSQL_DATABASE=dockds");

    protected final String driverClass;

    private final String image;

    private final UriComponents url;

    private final String username;

    private final String password;

    private final String[] env;

    DockerizedDatabase(String driverClass, String image, UriComponents url, String username, String password, String... env) {
        this.driverClass = driverClass;
        this.image = image;
        this.url = url;
        this.username = username;
        this.password = password;
        this.env = env;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public String getImage() {
        return image;
    }

    public UriComponents getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String[] getEnv() {
        return env;
    }
}
