package io.project.kvansiptobot.service.wrapper;

import io.project.kvansiptobot.service.TelegramBot;
import lombok.Builder;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SendMessageWrapper extends SendMessage implements BotApiMethodInterface {

  @Builder(builderClassName = "SendMessageWrapperBuilder", builderMethodName = "newBuilder")
  public static SendMessageWrapper create(Long chatId, String text, ReplyKeyboard replyMarkup, String parseMode,
      boolean disableWebPagePreview) {
    SendMessageWrapper sendMessageWrapper = new SendMessageWrapper();
    sendMessageWrapper.setChatId(chatId.toString());
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
