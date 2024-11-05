package kvansipto.telegram.microservice.services;

import java.util.List;
import java.util.function.Consumer;
import kvansipto.exercise.dto.UpdateDto;
import kvansipto.exercise.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.dto.TelegramActionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class KafkaTelegramService implements CommandLineRunner {

  @Value("${kafka.topic.messages}")
  private String messagesToExercisesTopicName;

  private final ApplicationEventPublisher eventPublisher;
  private final ReactiveKafkaConsumerTemplate<Long, BotApiMethodInterface> botApiReceiver;
  private final ReactiveKafkaConsumerTemplate<String, List<BotCommand>> botCommandReceiver;
  private final ReactiveKafkaProducerTemplate<Long, UpdateDto> updateDtoSender;

  @Autowired
  public KafkaTelegramService(ApplicationEventPublisher eventPublisher,
      ReactiveKafkaConsumerTemplate<Long, BotApiMethodInterface> botApiReceiver,
      ReactiveKafkaConsumerTemplate<String, List<BotCommand>> botCommandReceiver,
      ReactiveKafkaProducerTemplate<Long, UpdateDto> updateDtoSender) {
    this.eventPublisher = eventPublisher;
    this.botApiReceiver = botApiReceiver;
    this.botCommandReceiver = botCommandReceiver;
    this.updateDtoSender = updateDtoSender;
  }

  public Mono<Void> receiveUpdateDto() {
    return receiveAndProcess(botApiReceiver, value -> eventPublisher.publishEvent(new TelegramActionEvent(value)));
  }

  public Mono<Void> receivedCommandList() {
    return receiveAndProcess(botCommandReceiver,
        value -> eventPublisher.publishEvent(
            new SetMyCommands(value, new BotCommandScopeDefault(), null)));
  }

  private <K, T> Mono<Void> receiveAndProcess(ReactiveKafkaConsumerTemplate<K, T> receiver,
      Consumer<T> eventProcessor) {
    return receiver.receiveAutoAck()
        .doOnNext(consumerRecord -> {
          eventProcessor.accept(consumerRecord.value());
          log.info("Received message from topic: {} with key: {}, value: {}",
              consumerRecord.topic(), consumerRecord.key(), consumerRecord.value());
        })
        .doOnError(error -> log.error("Error receiving message: {}", error.getMessage()))
        .then();
  }

  public Mono<Void> sendUpdateDto(Long key, UpdateDto data) {
    String topic = messagesToExercisesTopicName;
    return updateDtoSender.send(topic, key, data)
        .doOnSuccess(
            result -> log.info("Message sent to kafka topic {}, key={}, data={}, result={}", topic, key, data, result))
        .doOnError(
            e -> log.error("Failed sending message to kafka topic {}, key={}, data={} with error message: {}", topic,
                key, data, e.getMessage()))
        .then();
  }

  @Override
  public void run(String... args) {
    receiveUpdateDto().subscribe();
    receivedCommandList().subscribe();
  }
}
