package io.project.kvansiptobot.service.command.menu;

import static io.project.kvansiptobot.service.command.menu.StartCommand.START_COMMAND_TEXT;

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

  //TODO Надо вынести в TelegramBot, чтобы генерилось динамически
  static final String HELP_TEXT = "This bot was made by Kvansipto\n\n"
      + "You can execute command from the main menu on the left or by typing a command:\n\n"
      + "Type" + START_COMMAND_TEXT + " to see welcome message\n\n"
//      + "Type" + EXERCISE_COMMAND_TEXT + " to see exercises and add results\n\n"
      + "Type" + HELP_COMMAND_TEXT + " to see this message again";

  @Override
  public boolean supports(Update update) {
    return update.getMessage().getText().equals(HELP_COMMAND_TEXT);
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    SendMessageWrapper sendMessage = new SendMessageWrapper();
    sendMessage.setChatId(update.getMessage().getChatId());
    sendMessage.setText(HELP_TEXT);
    return sendMessage;
  }

  @Override
  public String explanation() {
    return "how to use this bot";
  }
}
