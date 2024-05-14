package io.project.kvansiptobot.service.wrapper;

import io.project.kvansiptobot.service.TelegramBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface BotApiMethodInterface {

  void accept(TelegramBot bot) throws TelegramApiException;
}
