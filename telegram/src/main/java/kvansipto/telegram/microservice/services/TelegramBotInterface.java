package kvansipto.telegram.microservice.services;

import kvansipto.telegram.microservice.services.dto.TelegramActionEvent;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;

//TODO пришлось вернуть интерфейс, так как попытки переключиться на CGLib не увенчались успехом
/**
 * Failed to process @EventListener annotation on bean with name 'telegramBot': Need to invoke method
 * 'handleSetMyCommandEvent' declared on target class 'TelegramBot', but not found in any interface(s) of the exposed
 * proxy type. Either pull the method up to an interface or switch to CGLIB proxies by enforcing proxy-target-class mode
 * in your configuration.
 */
public interface TelegramBotInterface {

  void handleTelegramActionEvent(TelegramActionEvent telegramActionEvent);

  void handleSetMyCommandEvent(SetMyCommands event);
}
