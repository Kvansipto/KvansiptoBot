package microservice.service.command.menu;

import kvansipto.exercise.wrapper.BotApiMethodInterface;
import microservice.service.KafkaExerciseService;
import microservice.service.command.Command;
import microservice.service.event.UserInputCommandEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public abstract class MainMenuCommand extends Command {

  protected MainMenuCommand(KafkaTemplate<Long, BotApiMethodInterface> kafkaTemplate, KafkaExerciseService kafkaExerciseService) {
    super(kafkaTemplate, kafkaExerciseService);
  }

  @Override
  public boolean supports(UserInputCommandEvent update) {
    return update.update().getMessage().equals(this.getClass().getDeclaredAnnotation(Component.class).value());
  }

  public abstract String explanation();

  @Override
  public String toString() {
    return String.format("MainMenuCommand: %s with explanation %s", this.getClass().getSimpleName(),
        this.explanation());
  }
}
