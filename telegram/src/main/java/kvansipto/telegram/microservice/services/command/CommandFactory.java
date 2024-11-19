package kvansipto.telegram.microservice.services.command;

import jakarta.annotation.PostConstruct;
import kvansipto.telegram.microservice.services.command.menu.HelpCommand;
import kvansipto.telegram.microservice.services.command.menu.MainMenuCommand;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommandFactory {

    @Getter
    private final List<Command> commandList;

    @PostConstruct
    private void init(){
        StringBuilder helpText = new StringBuilder("This bot was made by Kvansipto\n\n");
        commandList.stream()
                .filter(MainMenuCommand.class::isInstance)
                .forEach(clazz -> {
                    helpText
                            .append("Type ")
                            .append(clazz.getClass().getAnnotation(Component.class).value())
                            .append(" ")
                            .append(clazz instanceof HelpCommand
                                    ? " to see this message again"
                                    : ((MainMenuCommand) clazz).explanation())
                            .append("\n\n");
                });
        commandList.stream()
                .filter(c -> c instanceof HelpCommand)
                .findFirst()
                .map(c -> (HelpCommand) c)
                .ifPresent(help -> help.setHelpText(helpText.toString()));
    }
}
