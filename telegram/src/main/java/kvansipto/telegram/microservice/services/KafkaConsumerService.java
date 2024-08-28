package kvansipto.telegram.microservice.services;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.dto.PageDto;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

  private final Map<Long, CompletableFuture<PageDto<ExerciseResultDto>>> responseMap = new ConcurrentHashMap<>();

  public PageDto<ExerciseResultDto> waitForResponse(Long chatId)
      throws ExecutionException, InterruptedException {
    CompletableFuture<PageDto<ExerciseResultDto>> future = new CompletableFuture<>();
    responseMap.put(chatId, future);
    return future.get();
  }

  @KafkaListener(topics = "${kafka.topic.response}")
  public void listenResponse(@Payload PageDto<ExerciseResultDto> response,
      @Header(KafkaHeaders.RECEIVED_KEY) Long chatId) {
    CompletableFuture<PageDto<ExerciseResultDto>> future = responseMap.get(chatId);
    if (future != null) {
      future.complete(response);
      responseMap.remove(chatId);
    }
  }
}
