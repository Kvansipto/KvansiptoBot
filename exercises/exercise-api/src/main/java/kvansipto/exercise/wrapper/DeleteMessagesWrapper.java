package kvansipto.exercise.wrapper;

import java.util.List;
import lombok.Builder;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessages;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class DeleteMessagesWrapper extends DeleteMessages implements BotApiMethodInterface {

  @Builder(builderClassName = "DeleteMessageWrapperBuilder", builderMethodName = "newBuilder")
  public static DeleteMessagesWrapper create(Long chatId, List<Integer> messageId) {
    DeleteMessagesWrapper deleteMessagesWrapper = new DeleteMessagesWrapper();
    deleteMessagesWrapper.setMessageIds(messageId);
    deleteMessagesWrapper.setChatId(chatId);
    return deleteMessagesWrapper;
  }

  @Override
  public void accept(TelegramLongPollingBot bot) throws TelegramApiException {
    bot.execute(this);
  }
}
