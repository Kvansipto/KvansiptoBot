package microservice.service;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import microservice.service.command.menu.CommandName;
import microservice.service.command.menu.HelpCommand;
import microservice.service.command.menu.MainMenuCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

@Component
@Slf4j
public class CommandInitializer {

  public static String HELP_TEXT;

//  @Autowired
//  private List<MainMenuCommand> mainMenuCommandList;

  List<BotCommand> commands;

//  @Autowired
//  private KafkaTemplate<String, List<BotCommand>> kafkaTemplate;

  @Autowired
  public CommandInitializer(List<MainMenuCommand> mainMenuCommandList,
      KafkaTemplate<String, List<BotCommand>> kafkaTemplate) {
//    this.mainMenuCommandList = mainMenuCommandList;
//    this.kafkaTemplate = kafkaTemplate;

    StringBuilder helpText = new StringBuilder("This bot was made by Kvansipto\n\n");
    commands = new ArrayList<>();
    //TODO Не работает
    mainMenuCommandList.forEach(command -> {
      CommandName commandNameAnnotation = command.getClass().getAnnotation(CommandName.class);
      if (commandNameAnnotation != null) {
        String commandName = commandNameAnnotation.value();
        helpText.append("Type ")
            .append(commandName)
            .append(" ")
            .append(command instanceof HelpCommand ? "to see this message again" : command.explanation())
            .append("\n\n");
        commands.add(new BotCommand(commandName, command.explanation()));
      }
    });

    HELP_TEXT = helpText.toString();
    kafkaTemplate.send("main-menu-commands", commands);
    log.info("Sent main-menu-commands {} to kafka topic {}", commands, "main-menu-commands");
  }

//  @PostConstruct
//  public void initializeCommands() {
//    StringBuilder helpText = new StringBuilder("This bot was made by Kvansipto\n\n");
//    commands = new ArrayList<>();
//
//    mainMenuCommandList.forEach(command -> {
//      CommandName commandNameAnnotation = command.getClass().getAnnotation(CommandName.class);
//      if (commandNameAnnotation != null) {
//        String commandName = commandNameAnnotation.value();
//        helpText.append("Type ")
//            .append(commandName)
//            .append(" ")
//            .append(command instanceof HelpCommand ? "to see this message again" : command.explanation())
//            .append("\n\n");
//        commands.add(new BotCommand(commandName, command.explanation()));
//      }
//    });
//
//    HELP_TEXT = helpText.toString();
//    kafkaTemplate.send("main-menu-commands", commands);
//    log.info("Sent main-menu-commands {} to kafka topic {}", commands, "main-menu-commands");
//  }
}

