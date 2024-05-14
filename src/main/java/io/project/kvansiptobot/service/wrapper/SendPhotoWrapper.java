package io.project.kvansiptobot.service.wrapper;

import io.project.kvansiptobot.service.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SendPhotoWrapper extends SendPhoto implements BotApiMethodInterface {

  @Override
  public void accept(TelegramBot bot) throws TelegramApiException {
    bot.execute(this);
  }
}
