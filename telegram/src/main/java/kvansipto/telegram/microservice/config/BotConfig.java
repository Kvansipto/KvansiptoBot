package kvansipto.telegram.microservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class BotConfig {

  @Autowired
  private Environment env;

  public String getBotToken() {
    return env.getProperty("telegram.bot.botToken");
  }

  public String getBotName() {
    return env.getProperty("telegram.bot.botName");
  }
}
