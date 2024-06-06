package kvansipto.telegram.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class TelegramApplication {

  public static void main(String[] args) {
    SpringApplication.run(TelegramApplication.class, args);
  }

  //TODO
  @Bean
  public RestTemplate getRestTemplate() {
    return new RestTemplate();
  }
}
