package kvansipto.exercise.wrapper;

import lombok.Builder;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class EditMessageWrapper extends EditMessageText implements BotApiMethodInterface {

  @Builder(builderClassName = "EditMessageWrapperBuilder", builderMethodName = "newBuilder")
  public static EditMessageWrapper create(Long chatId, Integer messageId, String text,
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
  public void accept(TelegramLongPollingBot bot) throws TelegramApiException {
    bot.execute(this);
  }
}
