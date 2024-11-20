package microservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import microservice.service.command.menu.CommandName;
import microservice.service.command.menu.HelpCommand;
import microservice.service.command.menu.MainMenuCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

@Component
@Slf4j
public class CommandInitializer {

  public static String HELP_TEXT;

  private final List<BotCommand> commands;
  private final KafkaExerciseService kafkaExerciseService;

  @Autowired
  public CommandInitializer(List<MainMenuCommand> mainMenuCommandList, KafkaExerciseService kafkaExerciseService) {
    this.kafkaExerciseService = kafkaExerciseService;

    StringBuilder helpText = new StringBuilder("This bot was made by Kvansipto\n\n");
    commands = new ArrayList<>();

    log.info("Received Main Menu commands: {}", mainMenuCommandList.stream()
        .map(MainMenuCommand::toString)
        .collect(Collectors.joining(", ")));

    mainMenuCommandList.forEach(command -> {
      CommandName commandNameAnnotation = command.getClass().getAnnotation(CommandName.class);
      if (commandNameAnnotation != null) {
        String commandName = commandNameAnnotation.value();
        //TODO Не инициализируется explanation комманд
        helpText.append("Type ")
            .append(commandName)
            .append(" ")
            .append(command instanceof HelpCommand ? "to see this message again" : command.explanation())
            .append("\n\n");
        commands.add(new BotCommand(commandName, command.explanation()));
      }
    });

    HELP_TEXT = helpText.toString();
    log.info("HELP TEXT: {}", HELP_TEXT);
    kafkaExerciseService.sendMainMenuCommands(commands).subscribe();
  }
}
