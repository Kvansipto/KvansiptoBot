package kvansipto.telegram.microservice.services.wrapper;

import kvansipto.telegram.microservice.services.TelegramBot;
import lombok.Builder;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SendMessageWrapper extends SendMessage implements BotApiMethodInterface {

  @Builder(builderClassName = "SendMessageWrapperBuilder", builderMethodName = "newBuilder")
  public static SendMessageWrapper create(String chatId, String text, ReplyKeyboard replyMarkup, String parseMode,
      boolean disableWebPagePreview) {
    SendMessageWrapper sendMessageWrapper = new SendMessageWrapper();
    sendMessageWrapper.setChatId(chatId);
    sendMessageWrapper.setText(text);
    sendMessageWrapper.setReplyMarkup(replyMarkup);
    sendMessageWrapper.setParseMode(parseMode);
    sendMessageWrapper.setDisableWebPagePreview(disableWebPagePreview);
    return sendMessageWrapper;
  }

  @Override
  public void accept(TelegramBot bot) throws TelegramApiException {
    bot.execute(this);
  }
}
