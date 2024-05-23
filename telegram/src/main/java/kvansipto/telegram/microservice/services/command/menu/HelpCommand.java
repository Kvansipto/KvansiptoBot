package io.project.kvansiptobot.service.command.menu;

import static io.project.kvansiptobot.service.TelegramBot.HELP_TEXT;

import io.project.kvansiptobot.repository.UserRepository;
import io.project.kvansiptobot.service.wrapper.BotApiMethodInterface;
import io.project.kvansiptobot.service.wrapper.SendMessageWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component("/help")
public class HelpCommand extends MainMenuCommand {

  @Autowired
  UserRepository userRepository;

  public static final String HELP_COMMAND_TEXT = "/help";

  @Override
  public boolean supports(Update update) {
    return update.getMessage().getText().equals(HELP_COMMAND_TEXT);
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    return SendMessageWrapper.newBuilder()
        .chatId(update.getMessage().getChatId())
        .text(HELP_TEXT)
        .build();
  }

  @Override
  public String explanation() {
    return "how to use this bot";
  }
}
