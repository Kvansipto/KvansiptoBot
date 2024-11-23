package microservice.service.command.menu;

import kvansipto.exercise.wrapper.BotApiMethodInterface;
import kvansipto.exercise.wrapper.SendMessageWrapper;
import lombok.Setter;
import microservice.service.KafkaExerciseService;
import microservice.service.event.UserInputCommandEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Setter
@Component("/help")
public class HelpCommand extends MainMenuCommand {

  private String helpText;

  //TODO пришлось добавить конструктор для тестов, потому что не хочу инжектить эти бины,
  // так как они цепляют много бинов, которые не нужны для проверки
  public HelpCommand(KafkaTemplate<Long, BotApiMethodInterface> kafkaTemplate,
      KafkaExerciseService kafkaExerciseService) {
    super(kafkaTemplate, kafkaExerciseService);
  }

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
