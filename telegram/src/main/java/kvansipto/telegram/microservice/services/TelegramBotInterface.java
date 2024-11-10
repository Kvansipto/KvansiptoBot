package kvansipto.telegram.microservice.services;

import kvansipto.telegram.microservice.services.dto.TelegramActionEvent;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;

/*
Caused by: java.lang.IllegalStateException: Need to invoke method 'handleTelegramActionEvent' declared on target class 'TelegramBot', but not found in any interface(s) of the exposed proxy type. Either pull the method up to an interface or switch to CGLIB proxies by enforcing proxy-target-class mode in your configuration.
 */
public interface TelegramBotInterface {

  void handleSetMyCommandEvent(SetMyCommands event);

  void handleTelegramActionEvent(TelegramActionEvent event);
}