package pl.touk.dockds;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@org.springframework.context.annotation.Configuration
@EnableJpaRepositories
@EnableAutoConfiguration
@Import(DockerizedDataSourceAutoConfiguration.class)
public class SampleConfiguration {

}
