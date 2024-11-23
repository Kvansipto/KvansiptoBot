package microservice.service.command;

import kvansipto.exercise.wrapper.BotApiMethodInterface;
import lombok.RequiredArgsConstructor;
import microservice.service.KafkaExerciseService;
import microservice.service.event.UserInputCommandEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public abstract class Command {

  protected final KafkaTemplate<Long, BotApiMethodInterface> kafkaTemplate;
  protected final KafkaExerciseService kafkaExerciseService;

  public abstract boolean supports(UserInputCommandEvent update);

  @EventListener
  @Async
  public void onEvent(UserInputCommandEvent event) {
    if (supports(event)) {
      this.process(event);
    }
  }

  public abstract void process(UserInputCommandEvent command);
}
