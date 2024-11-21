package kvansipto.exercise.wrapper;

import lombok.Builder;
import lombok.NonNull;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SendMessageWrapper extends SendMessage implements BotApiMethodInterface {

  @Builder(builderClassName = "SendMessageWrapperBuilder", builderMethodName = "newBuilder")
  public static SendMessageWrapper create(Long chatId, String text, ReplyKeyboard replyMarkup, String parseMode,
      boolean disableWebPagePreview) {
    var sendMessageWrapper = new SendMessageWrapper();
    sendMessageWrapper.setChatId(chatId);
    sendMessageWrapper.setText(text);
    sendMessageWrapper.setReplyMarkup(replyMarkup);
    sendMessageWrapper.setParseMode(parseMode);
    sendMessageWrapper.setDisableWebPagePreview(disableWebPagePreview);
    return sendMessageWrapper;
  }

  @Override
  public void accept(TelegramLongPollingBot bot) throws TelegramApiException {
    bot.execute(this);
  }

  public @NonNull Long getNumberChatId() {
    return Long.valueOf(super.getChatId());
  }
}
