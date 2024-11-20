package microservice.service.command.menu;

import kvansipto.exercise.wrapper.SendMessageWrapper;
import microservice.service.CommandInitializer;
import microservice.service.event.UserInputCommandEvent;
import org.springframework.stereotype.Component;

@Component
@CommandName("/help")
public class HelpCommand extends MainMenuCommand {

  @Override
  public void process(UserInputCommandEvent event) {
    kafkaExerciseService.sendBotApiMethod(event.chatId(),
            SendMessageWrapper.newBuilder()
                .chatId(event.chatId())
                .text(CommandInitializer.HELP_TEXT)
                .build())
        .subscribe();
  }

  @Override
  public String explanation() {
    return "how to use this bot";
  }
}
