package pl.touk.dockds;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
public class DockerizedDataSourceAutoConfiguration implements BeanClassLoaderAware {

    private ClassLoader classLoader;

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource", name = "url", havingValue = "false", matchIfMissing = true)
    public DataSource dataSource() throws Exception {
        DockerizedDatabase databaseConnection = Arrays.stream(DockerizedDatabase.values())
                .filter(ddc -> ClassUtils.isPresent(ddc.getDriverClass(), classLoader))
                .findFirst()
                .orElseThrow(ClassNotFoundException::new);
        return new DockerizedDataSource(databaseConnection);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
