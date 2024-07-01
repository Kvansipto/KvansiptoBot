package microservice.service.postgre;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(initializers = AbstractPostgreTestContainerTestBase.Initializer.class)
public class AbstractPostgreTestContainerTestBase {

  public static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:latest")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  static {
    postgreSQLContainer.start();
  }

  static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
      TestPropertyValues.of(
          "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
          "spring.datasource.username=" + postgreSQLContainer.getUsername(),
          "spring.datasource.password=" + postgreSQLContainer.getPassword()
      ).applyTo(configurableApplicationContext.getEnvironment());
    }
  }
}
