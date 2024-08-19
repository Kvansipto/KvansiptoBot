package kvansipto.telegram.microservice.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import kvansipto.exercise.dto.ExerciseResultDto;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

  private final Map<Long, CompletableFuture<List<ExerciseResultDto>>> responseMap = new ConcurrentHashMap<>();

  public List<ExerciseResultDto> waitForResponse(Long chatId)
      throws ExecutionException, InterruptedException {
    CompletableFuture<List<ExerciseResultDto>> future = new CompletableFuture<>();
    responseMap.put(chatId, future);
    return future.get();
  }

  @KafkaListener(topics = "response-from-exercises", groupId = "server.exercise.results")
  public void listenResponse(@Payload List<ExerciseResultDto> response,
      @Header(KafkaHeaders.RECEIVED_KEY) Long chatId) {
    CompletableFuture<List<ExerciseResultDto>> future = responseMap.get(chatId);
    if (future != null) {
      future.complete(response);
      responseMap.remove(chatId);
    }
  }
}
