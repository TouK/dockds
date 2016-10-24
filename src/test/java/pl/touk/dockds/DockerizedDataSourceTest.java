package pl.touk.dockds;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { DockerizedDataSourceTest.TestConfiguration.class})
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.hbm2ddl.auto:create"
})
public class DockerizedDataSourceTest {

    @Resource
    private DataSource dataSource;

    @Resource
    private SampleRepository sampleRepository;

    @Test
    public void shouldProvideValidJdbcConnection() throws SQLException {
        //when
        Connection connection = dataSource.getConnection();
        //then
        Assertions.assertThat(connection.isValid(100));
    }

    @Test
    public void shouldSupportJpa() {
        //given
        SampleEntity sampleEntity = SampleEntity.builder().id(1L).value("test1").build();
        //when
        sampleRepository.save(sampleEntity);
        //then
        Assertions.assertThat(sampleRepository.findOne(1L).getValue()).isEqualTo(sampleEntity.getValue());
    }

    @org.springframework.context.annotation.Configuration
    @EnableJpaRepositories
    @EnableAutoConfiguration
    @Import(DockerizedDataSourceAutoConfiguration.class)
    public static class TestConfiguration {

    }
}