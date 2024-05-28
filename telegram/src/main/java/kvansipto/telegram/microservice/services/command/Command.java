package kvansipto.telegram.microservice.services.command;

import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public abstract class Command {

  public abstract boolean supports(Update update);

  public abstract BotApiMethodInterface process(Update update);
}
