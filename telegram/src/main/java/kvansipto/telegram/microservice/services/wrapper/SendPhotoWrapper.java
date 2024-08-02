package kvansipto.telegram.microservice.services.wrapper;

import kvansipto.telegram.microservice.services.TelegramBot;
import lombok.Builder;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SendPhotoWrapper extends SendPhoto implements BotApiMethodInterface {

  @Builder(builderClassName = "SendPhotoWrapperBuilder", builderMethodName = "newBuilder")
  public static SendPhotoWrapper create(Long chatId, InputFile photo, String caption) {
    SendPhotoWrapper sendPhotoWrapper = new SendPhotoWrapper();
    sendPhotoWrapper.setChatId(chatId);
    sendPhotoWrapper.setPhoto(photo);
    sendPhotoWrapper.setCaption(caption);
    return sendPhotoWrapper;
  }

  @Override
  public void accept(TelegramBot bot) throws TelegramApiException {
    bot.execute(this);
  }
}
