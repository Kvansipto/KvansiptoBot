package kvansipto.telegram.microservice.services.command.menu;

import kvansipto.telegram.microservice.services.UserStateService;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.EditMessageWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component("/stop")
public class StopCommand extends MainMenuCommand {

  @Autowired
  UserStateService userStateService;

  public static final String SAVE_RESULT_ANSWER_TEXT = "Все выполняемые команды остановлены";

  @Override
  public boolean supports(Update update) {
    return update.hasMessage() && update.getMessage().getText().equals("/stop");
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    var chatId = update.getMessage().getChatId();
    userStateService.removeUserState(chatId);

    return EditMessageWrapper.newBuilder()
        .chatId(chatId)
        .messageId(update.getMessage().getMessageId())
        .text(SAVE_RESULT_ANSWER_TEXT)
        .build();
  }

  @Override
  public String explanation() {
    return "To stop the execution of all commands (removing the user's active state)";
  }
}
