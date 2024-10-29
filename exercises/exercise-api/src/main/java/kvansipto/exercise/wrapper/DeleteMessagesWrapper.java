package kvansipto.exercise.wrapper;

import java.util.List;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessages;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class DeleteMessagesWrapper extends DeleteMessages implements BotApiMethodInterface {

  public DeleteMessagesWrapper(Long chatId, List<Integer> messageIds) {
    super.setChatId(chatId);
    super.setMessageIds(messageIds);
  }

  @Override
  public void accept(TelegramLongPollingBot bot) throws TelegramApiException {
    bot.execute(this);
  }
}
