package kvansipto.telegram.microservice.services.command.menu;

import static kvansipto.telegram.microservice.services.TelegramBot.HELP_TEXT;

import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.SendMessageWrapper;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component("/help")
public class HelpCommand extends MainMenuCommand {

  public static final String HELP_COMMAND_TEXT = "/help";

  @Override
  public boolean supports(Update update) {
    return update.hasMessage() && update.getMessage().getText().equals(HELP_COMMAND_TEXT);
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    return SendMessageWrapper.newBuilder()
        .chatId(update.getMessage().getChatId().toString())
        .text(HELP_TEXT)
        .build();
  }

  @Override
  public String explanation() {
    return "how to use this bot";
  }
}
