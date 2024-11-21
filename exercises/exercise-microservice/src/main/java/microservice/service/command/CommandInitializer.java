package microservice.service.command;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import microservice.service.KafkaExerciseService;
import microservice.service.command.menu.CommandName;
import microservice.service.command.menu.HelpCommand;
import microservice.service.command.menu.MainMenuCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

@Component
@Slf4j
public class CommandInitializer {

  //  private final List<BotCommand> commands;
  @Getter
  private final List<Command> commandList;

  @Autowired
  public CommandInitializer(
      List<Command> commandList,
      KafkaExerciseService kafkaExerciseService) {
    this.commandList = commandList;

    StringBuilder helpText = new StringBuilder("This bot was made by Kvansipto\n\n");
    List<BotCommand> botCommands = new ArrayList<>();
    commandList.stream()
        .filter(MainMenuCommand.class::isInstance)
        .forEach(clazz -> {
          var commandNameValue = clazz.getClass().getAnnotation(CommandName.class).value();
          var explanation = ((MainMenuCommand) clazz).explanation();
          helpText
              .append("Type ")
              .append(commandNameValue)
              .append(" ")
              .append(clazz instanceof HelpCommand
                  ? " to see this message again"
                  : explanation)
              .append("\n\n");
          botCommands.add(new BotCommand(commandNameValue, explanation));
        })
    ;
//    commands = new ArrayList<>();

//    log.info("Received Main Menu commands: {}", mainMenuCommandList.stream()
//        .map(MainMenuCommand::toString)
//        .collect(Collectors.joining(", ")));

//    mainMenuCommandList.forEach(command -> {
//      CommandName commandNameAnnotation = command.getClass().getAnnotation(CommandName.class);
//      if (commandNameAnnotation != null) {
//        String commandName = commandNameAnnotation.value();
//        //TODO Не инициализируется explanation комманд
//        helpText.append("Type ")
//            .append(commandName)
//            .append(" ")
//            .append(command instanceof HelpCommand ? "to see this message again" : command.explanation())
//            .append("\n\n");
//        commands.add(new BotCommand(commandName, command.explanation()));
//      }
//    });
    commandList.stream()
        .filter(HelpCommand.class::isInstance)
        .findFirst()
        .map(c -> (HelpCommand) c)
        .ifPresent(help -> help.setHelpText(helpText.toString()));

    kafkaExerciseService.sendMainMenuCommands(botCommands).subscribe();
  }
}
