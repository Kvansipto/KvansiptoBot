package io.project.kvansiptobot.service.command.menu;

import io.project.kvansiptobot.service.command.Command;
import org.springframework.stereotype.Component;

@Component
public abstract class MainMenuCommand extends Command {

  public abstract String explanation();
}
