package kvansipto.telegram.microservice.services.wrapper;

import java.util.List;
import kvansipto.telegram.microservice.services.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessages;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class DeleteMessagesWrapper extends DeleteMessages implements BotApiMethodInterface {

  public DeleteMessagesWrapper(String chatId, List<Integer> messageIds) {
    super.setChatId(chatId);
    super.setMessageIds(messageIds);
  }

  @Override
  public void accept(TelegramBot bot) throws TelegramApiException {
    bot.execute(this);
  }
}
