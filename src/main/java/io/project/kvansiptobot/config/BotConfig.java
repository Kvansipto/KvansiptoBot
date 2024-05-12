package io.project.kvansiptobot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Data
@EnableScheduling
@PropertySource("application.properties")
@ComponentScan
public class BotConfig {

  @Value("${bot.name}")
  String botName;
  @Value("${bot.token}")
  String botToken;
  @Value("${bot.owner}")
  Long ownerChatId;
}
