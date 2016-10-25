package pl.touk.dockds;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = SampleConfiguration.class)
@DockerizedDataJpaTest
public class DockerizedDataJpaTestAnnotationTest {

    @Resource
    private DataSource dataSource;

    @Test
    public void shouldProvideDatasource() throws SQLException {
        Assertions.assertThat(dataSource).isInstanceOf(DockerizedDataSource.class);
    }

}
