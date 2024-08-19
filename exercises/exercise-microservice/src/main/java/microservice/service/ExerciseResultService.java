package microservice.service;

import com.querydsl.core.types.Predicate;
import java.util.List;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.filter.ExerciseResultFilter;
import lombok.extern.slf4j.Slf4j;
import microservice.entity.ExerciseResult;
import microservice.mapper.ExerciseMapper;
import microservice.mapper.ExerciseResultMapper;
import microservice.repository.ExerciseResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExerciseResultService extends
    BaseMappedService<ExerciseResult, ExerciseResultDto, Long, ExerciseResultRepository, ExerciseResultMapper> {

  private final ExerciseMapper exerciseMapper;

  @Autowired
  private ExerciseResultPredicateBuilder predicateBuilder;

  private final KafkaTemplate<Long, List<ExerciseResultDto>> kafkaTemplate;

  protected ExerciseResultService(ExerciseResultRepository repository, ExerciseResultMapper mapper,
      ExerciseMapper exerciseMapper, KafkaTemplate<Long, List<ExerciseResultDto>> kafkaTemplate) {
    super(repository, mapper);
    this.exerciseMapper = exerciseMapper;
    this.kafkaTemplate = kafkaTemplate;
  }

  @KafkaListener(topics = "request-to-exercises", groupId = "server.exercise.results")
  public void processExerciseRequest(@Payload ExerciseResultFilter filter,
      @Header(KafkaHeaders.RECEIVED_KEY) Long chatId) {
    Pageable pageable = PageRequest.of(0, 20);
    Predicate predicate = predicateBuilder.apply(filter);
    List<ExerciseResultDto> exerciseResultDtoList =
        repository.findAll(predicate, pageable).stream().map(mapper::toDto).toList();
//    Page<ExerciseResultDto> exerciseResultDtoPage = new PageImpl<>(exerciseResultDtoList, pageable,
//        exerciseResultDtoList.size());

    // Отправляем ответ в ответный топик
    kafkaTemplate.send("response-from-exercises", chatId, exerciseResultDtoList);
    log.info("sent to kafka topic: {}, {}", "response-from-exercises", exerciseResultDtoList);
  }
}
