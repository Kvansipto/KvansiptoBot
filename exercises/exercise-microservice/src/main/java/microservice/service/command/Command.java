package microservice.service.command;

import kvansipto.exercise.wrapper.BotApiMethodInterface;
import microservice.service.KafkaService;
import microservice.service.event.UserInputCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public abstract class Command {

  @Autowired
  protected KafkaTemplate<Long, BotApiMethodInterface> kafkaTemplate;

  @Autowired
  protected KafkaService kafkaService;

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
