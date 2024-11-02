package kvansipto.telegram.microservice.services;

import java.util.List;
import kvansipto.telegram.microservice.services.dto.TelegramActionEvent;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

/*
Caused by: java.lang.IllegalStateException: Need to invoke method 'handleTelegramActionEvent' declared on target class 'TelegramBot', but not found in any interface(s) of the exposed proxy type. Either pull the method up to an interface or switch to CGLIB proxies by enforcing proxy-target-class mode in your configuration.
 */
public interface TelegramBotInterface {
  void handleTelegramActionEvent(TelegramActionEvent event);
  void receivedCommandList(List<BotCommand> commands);
}
