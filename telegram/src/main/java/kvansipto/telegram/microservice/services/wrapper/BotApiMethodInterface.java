package kvansipto.telegram.microservice.services.wrapper;

import kvansipto.telegram.microservice.services.TelegramBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface BotApiMethodInterface {

  void accept(TelegramBot bot) throws TelegramApiException;
}
