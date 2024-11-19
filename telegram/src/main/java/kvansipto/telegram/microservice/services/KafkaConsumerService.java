package kvansipto.telegram.microservice.services;

import java.util.Comparator;
import java.util.List;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.dto.PageDto;
import kvansipto.telegram.microservice.services.dto.ExerciseResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

  private final ApplicationEventPublisher eventPublisher;

  @KafkaListener(topics = "${kafka.topic.response}")
  public void listenResponse(@Payload PageDto<ExerciseResultDto> response,
      @Header(KafkaHeaders.RECEIVED_KEY) Long chatId) {
    List<ExerciseResultDto> result = response.getContent();
    log.info("got exercise result: {}", result);
    result.sort(Comparator.comparing(ExerciseResultDto::getDate).reversed());

    ExerciseResultEvent event = new ExerciseResultEvent(chatId, result);
    eventPublisher.publishEvent(event);
  }
}
