package kvansipto.telegram.microservice.services.wrapper;

import kvansipto.telegram.microservice.services.TelegramBot;
import lombok.Builder;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class EditMessageWrapper extends EditMessageText implements BotApiMethodInterface {

  @Builder(builderClassName = "EditMessageWrapperBuilder", builderMethodName = "newBuilder")
  public static EditMessageWrapper create(String chatId, Integer messageId, String text,
      InlineKeyboardMarkup replyMarkup, String parseMode, boolean disableWebPagePreview) {
    EditMessageWrapper editMessageWrapper = new EditMessageWrapper();
    editMessageWrapper.setChatId(chatId);
    editMessageWrapper.setMessageId(messageId);
    editMessageWrapper.setText(text);
    editMessageWrapper.setReplyMarkup(replyMarkup);
    editMessageWrapper.setParseMode(parseMode);
    editMessageWrapper.setDisableWebPagePreview(disableWebPagePreview);
    return editMessageWrapper;
  }

  @Override
  public void accept(TelegramBot bot) throws TelegramApiException {
    bot.execute(this);
  }
}
