package microservice.service;

import java.util.List;
import kvansipto.exercise.dto.UpdateDto;
import kvansipto.exercise.wrapper.BotApiMethodInterface;
import lombok.extern.slf4j.Slf4j;
import microservice.service.event.UserInputCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class KafkaExerciseService implements CommandLineRunner {

  private final ApplicationEventPublisher eventPublisher;
  private final ReactiveKafkaProducerTemplate<Long, BotApiMethodInterface> botApiMethodSender;
  private final ReactiveKafkaProducerTemplate<String, List<BotCommand>> botCommandSender;
  private final ReactiveKafkaConsumerTemplate<Long, UpdateDto> updateDtoReceiver;
  private final ReactiveKafkaProducerTemplate<Long, String> mediaSender;

  @Value("${kafka.topic.messages}")
  private String messagesTopic;
  @Value("${kafka.topic.main-menu-commands}")
  private String mainMenuCommandTopic;
  @Value("${kafka.topic.actions}")
  private String actionsTopic;
  @Value("${kafka.topic.media}")
  private String mediaTopic;

  @Autowired
  public KafkaExerciseService(ApplicationEventPublisher eventPublisher,
      ReactiveKafkaProducerTemplate<Long, BotApiMethodInterface> botApiMethodSender,
      ReactiveKafkaProducerTemplate<String, List<BotCommand>> botCommandSender,
      ReactiveKafkaConsumerTemplate<Long, UpdateDto> updateDtoReceiver,
      ReactiveKafkaProducerTemplate<Long, String> mediaSender) {
    this.eventPublisher = eventPublisher;
    this.botApiMethodSender = botApiMethodSender;
    this.botCommandSender = botCommandSender;
    this.updateDtoReceiver = updateDtoReceiver;
    this.mediaSender = mediaSender;
  }

  private <K, V> Mono<Void> sendMessage(ReactiveKafkaProducerTemplate<K, V> sender, String topic, @Nullable K key,
      V data) {
    return sender.send(topic, key, data)
        .doOnSuccess(result -> log.info("Message sent to kafka topic {}, key={}, data={}, result={}",
            topic, key, data, result))
        .doOnError(e -> log.error("Failed sending message to kafka topic {}, key={}, data={} with error message: {}",
            topic, key, data, e.getMessage()))
        .then();
  }

  public Mono<Void> sendBotApiMethod(Long key, BotApiMethodInterface data) {
    return sendMessage(botApiMethodSender, actionsTopic, key, data);
  }

  public Mono<Void> sendMainMenuCommands(List<BotCommand> data) {
    return sendMessage(botCommandSender, mainMenuCommandTopic, null, data);
  }

  public Mono<Void> sendMedia(Long chatId, String serializedImage) {
    return sendMessage(mediaSender, mediaTopic, chatId, serializedImage);
  }

  public Mono<Void> receiveUpdateDto() {
    return updateDtoReceiver.receiveAutoAck()
        .doOnNext(consumerRecord -> {
          eventPublisher.publishEvent(new UserInputCommandEvent(this, consumerRecord.key(), consumerRecord.value()));
          log.info("Received message from topic: {} with key: {}, value: {}", consumerRecord.topic(),
              consumerRecord.key(),
              consumerRecord.value());
        })
        .doOnError(error -> log.error("Error receiving message: {}", error.getMessage()))
        .then();
  }

  @Override
  public void run(String... args) {
    receiveUpdateDto().subscribe();
  }
}
