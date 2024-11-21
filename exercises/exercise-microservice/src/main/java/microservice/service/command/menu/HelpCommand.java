package microservice.service.command.menu;

import kvansipto.exercise.wrapper.SendMessageWrapper;
import lombok.Setter;
import microservice.service.event.UserInputCommandEvent;
import org.springframework.stereotype.Component;

@Setter
@Component
@CommandName("/help")
public class HelpCommand extends MainMenuCommand {

  private String helpText;

  @Override
  public void process(UserInputCommandEvent event) {
    kafkaExerciseService.sendBotApiMethod(event.chatId(),
            SendMessageWrapper.newBuilder()
                .chatId(event.chatId())
                .text(helpText)
                .build())
        .subscribe();
  }

  @Override
  public String explanation() {
    return "how to use this bot";
  }
}
