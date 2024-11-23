package microservice.service.command;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import microservice.service.KafkaExerciseService;
import microservice.service.command.menu.HelpCommand;
import microservice.service.command.menu.MainMenuCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

@Getter
@Component
@Slf4j
public class CommandInitializer {

  private final List<MainMenuCommand> commandList;

  @Autowired
  public CommandInitializer(
      List<MainMenuCommand> commandList,
      KafkaExerciseService kafkaExerciseService) {
    this.commandList = commandList;

    var helpText = new StringBuilder("This bot was made by Kvansipto\n\n");
    List<BotCommand> botCommands = new ArrayList<>();
    commandList
        .forEach(clazz -> {
          var commandNameValue = AnnotationUtils.findAnnotation(clazz.getClass(), Component.class).value();
          var explanation = clazz.explanation();
          helpText
              .append("Type ")
              .append(commandNameValue)
              .append(" ")
              .append(clazz instanceof HelpCommand
                  ? " to see this message again"
                  : explanation)
              .append("\n\n");
          botCommands.add(new BotCommand(commandNameValue, explanation));
        });
    commandList.stream()
        .filter(HelpCommand.class::isInstance)
        .findFirst()
        .map(c -> (HelpCommand) c)
        .ifPresent(help -> help.setHelpText(helpText.toString()));

    kafkaExerciseService.sendMainMenuCommands(botCommands).subscribe();
  }
}
