package io.project.kvansiptobot.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@Valid
@ConfigurationProperties("telegram.bot")
public class BotConfig {

  @NotBlank
  String botName;
  @NotBlank
  String botToken;
  @NotBlank
  Long ownerChatId;
}
