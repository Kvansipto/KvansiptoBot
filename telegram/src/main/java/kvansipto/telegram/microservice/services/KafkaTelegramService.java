package kvansipto.telegram.microservice.services;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;
import kvansipto.exercise.dto.UpdateDto;
import kvansipto.exercise.wrapper.BotApiMethodInterface;
import kvansipto.exercise.wrapper.SendPhotoWrapper;
import kvansipto.telegram.microservice.services.dto.TelegramActionEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.InputFile;
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
  private final ReactiveKafkaConsumerTemplate<Long, String> mediaReceiver;

  @Autowired
  public KafkaTelegramService(ApplicationEventPublisher eventPublisher,
      ReactiveKafkaConsumerTemplate<Long, BotApiMethodInterface> botApiReceiver,
      ReactiveKafkaConsumerTemplate<String, List<BotCommand>> botCommandReceiver,
      ReactiveKafkaProducerTemplate<Long, UpdateDto> updateDtoSender,
      ReactiveKafkaConsumerTemplate<Long, String> mediaReceiver) {
    this.eventPublisher = eventPublisher;
    this.botApiReceiver = botApiReceiver;
    this.botCommandReceiver = botCommandReceiver;
    this.updateDtoSender = updateDtoSender;
    this.mediaReceiver = mediaReceiver;
  }

  public Mono<Void> receiveUpdateDto() {
    return receiveAndProcess(botApiReceiver, consumerRecord -> {
      eventPublisher.publishEvent(new TelegramActionEvent(consumerRecord.value()));
      return Mono.empty();
    });
  }

  public Mono<Void> receivedCommandList() {
    return receiveAndProcess(botCommandReceiver, consumerRecord -> {
      eventPublisher.publishEvent(new SetMyCommands(consumerRecord.value(), new BotCommandScopeDefault(), null));
      return Mono.empty();
    });
  }

  public Mono<Void> receivedMedia() {
    return receiveAndProcess(mediaReceiver, this::processMediaRecord);
  }

  private <K, T> Mono<Void> receiveAndProcess(ReactiveKafkaConsumerTemplate<K, T> receiver,
      Function<ConsumerRecord<K, T>, Mono<Void>> eventProcessor) {
    return receiver.receiveAutoAck()
        .doOnNext(consumerRecord -> log.info("Received message from topic: {} with key: {}, value: {}",
            consumerRecord.topic(), consumerRecord.key(), consumerRecord.value()))
        .flatMap(consumerRecord -> eventProcessor.apply(consumerRecord)
            .doOnError(error -> log.error("Error processing message: {}", error.getMessage())))
        .doOnError(error -> log.error("Error receiving message: {}", error.getMessage()))
        .then();
  }

  private Mono<Void> processMediaRecord(ConsumerRecord<Long, String> consumerRecord) {
    return Mono.fromRunnable(() -> {
      try {
        byte[] imageBytes = Base64.getDecoder().decode(consumerRecord.value());
        InputStream is = new ByteArrayInputStream(imageBytes);
        SendPhotoWrapper sendPhotoWrapper = SendPhotoWrapper.newBuilder()
            .chatId(consumerRecord.key())
            .photo(new InputFile(is, "table.png"))
            .build();
        eventPublisher.publishEvent(new TelegramActionEvent(sendPhotoWrapper));
      } catch (Exception e) {
        log.error("Error processing media record: {}", e.getMessage());
        throw new RuntimeException(e);
      }
    });
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
    receivedMedia().subscribe();
  }
}
