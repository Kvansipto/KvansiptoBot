package microservice.service.command.menu;

import microservice.service.command.Command;
import microservice.service.event.UserInputCommandEvent;
import org.springframework.stereotype.Component;

@Component
public abstract class MainMenuCommand extends Command {

  @Override
  public boolean supports(UserInputCommandEvent update) {
    return update.update().getMessage().equals(this.getClass().getDeclaredAnnotation(CommandName.class).value());
  }

  public abstract String explanation();

  @Override
  public String toString() {
    return String.format("MainMenuCommand: %s with explanation %s", this.getClass().getSimpleName(),
        this.explanation());
  }
}
