package microservice.service;

import kvansipto.exercise.dto.UpdateDto;
import lombok.extern.slf4j.Slf4j;
import microservice.service.event.UserInputCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.lang.Nullable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaService {

  private final ApplicationEventPublisher eventPublisher;

  @Value("${kafka.topic.messages}")
  private static String messagesTopic;
  @Value("${kafka.topic.main-menu-commands}")
  private static String mainMenuCommandTopic;

  @Autowired
  public KafkaService(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  public <K, V> void send(String topic, K key, @Nullable V data, KafkaTemplate<K, V> kafkaTemplate) {
    log.info("Sent message to kafka topic {}: {} {}", topic, key, data);
    kafkaTemplate.send(topic, key, data);
  }

  @KafkaListener(topics = "${kafka.topic.messages}", groupId = "${kafka.group.id.messages}",
      containerFactory = "updateDtoKafkaListenerFactory")
  public void listenMessagesTopic(@Payload UpdateDto update,
      @Header(KafkaHeaders.RECEIVED_KEY) Long chatId) {
    eventPublisher.publishEvent(new UserInputCommandEvent(this, chatId, update));
  }
}
