package microservice.service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import microservice.service.command.menu.HelpCommand;
import microservice.service.command.menu.MainMenuCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class CommandInitializer {

  public static String HELP_TEXT;
  @Getter
  private static final List<MainMenuCommand> mainMenuCommandList = new ArrayList<>();

  @Autowired
  private ApplicationContext applicationContext;

  @PostConstruct
  public void initializeCommands() {
    StringBuilder helpText = new StringBuilder("This bot was made by Kvansipto\n\n");

    applicationContext.getBeansOfType(MainMenuCommand.class).values().forEach(command -> {
      String commandName = command.getClass().getAnnotation(Component.class).value();
      mainMenuCommandList.add(command);
      helpText.append("Type ")
          .append(commandName)
          .append(" ")
          .append(command instanceof HelpCommand ? "to see this message again" : command.explanation())
          .append("\n\n");
    });

    HELP_TEXT = helpText.toString();
  }
}

