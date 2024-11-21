package kvansipto.exercise.wrapper;

import lombok.Builder;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SendPhotoWrapper extends SendPhoto implements BotApiMethodInterface {

  @Builder(builderClassName = "SendPhotoWrapperBuilder", builderMethodName = "newBuilder")
  public static SendPhotoWrapper create(Long chatId, InputFile photo, String caption) {
    var sendPhotoWrapper = new SendPhotoWrapper();
    sendPhotoWrapper.setChatId(chatId);
    sendPhotoWrapper.setPhoto(photo);
    sendPhotoWrapper.setCaption(caption);
    return sendPhotoWrapper;
  }

  @Override
  public void accept(TelegramLongPollingBot bot) throws TelegramApiException {
    bot.execute(this);
  }
}
