package microservice.service;

import kvansipto.exercise.dto.UpdateDto;
import lombok.extern.slf4j.Slf4j;
import microservice.service.event.UserInputCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumerService {

  private final ApplicationEventPublisher eventPublisher;

  @Autowired
  public KafkaConsumerService(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @KafkaListener(topics = "${kafka.topic.messages}")
  public void listenMessagesTopic(@Payload UpdateDto update,
      @Header(KafkaHeaders.RECEIVED_KEY) Long chatId) {
    eventPublisher.publishEvent(new UserInputCommandEvent(this, chatId, update));
  }
}
