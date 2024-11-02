package kvansipto.telegram.microservice.services;

import kvansipto.exercise.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.dto.TelegramActionEvent;
import lombok.extern.slf4j.Slf4j;
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

  @KafkaListener(topics = "${kafka.topic.actions}", groupId = "${kafka.group.id.actions}",
      containerFactory = "botApiMethodKafkaListenerFactory")
  public void listenCommandAction(@Payload BotApiMethodInterface action,
      @Header(KafkaHeaders.RECEIVED_KEY) Long chatId) {
    log.info("Received command action: {}", action);
    eventPublisher.publishEvent(new TelegramActionEvent(action));
  }
}
