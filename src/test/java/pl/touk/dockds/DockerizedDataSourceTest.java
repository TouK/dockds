package pl.touk.dockds;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        SampleConfiguration.class},
        properties = {
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

}