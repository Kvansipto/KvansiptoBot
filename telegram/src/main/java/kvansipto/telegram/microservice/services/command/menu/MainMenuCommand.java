package kvansipto.telegram.microservice.services.command.menu;

import kvansipto.telegram.microservice.services.command.Command;
import org.springframework.stereotype.Component;

@Component
public abstract class MainMenuCommand extends Command {

  public abstract String explanation();
}
