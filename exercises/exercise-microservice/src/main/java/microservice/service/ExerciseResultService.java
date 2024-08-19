package microservice.service;

import com.querydsl.core.types.Predicate;
import java.util.List;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.dto.PageDto;
import kvansipto.exercise.filter.ExerciseResultFilter;
import lombok.extern.slf4j.Slf4j;
import microservice.entity.ExerciseResult;
import microservice.mapper.ExerciseResultMapper;
import microservice.repository.ExerciseResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

  @Value("${kafka.topic.response}")
  private String exerciseResultTopicResponse;

  @Autowired
  private ExerciseResultPredicateBuilder predicateBuilder;

  private final KafkaTemplate<Long, PageDto<ExerciseResultDto>> kafkaTemplate;

  protected ExerciseResultService(ExerciseResultRepository repository, ExerciseResultMapper mapper,
      KafkaTemplate<Long, PageDto<ExerciseResultDto>> kafkaTemplate) {
    super(repository, mapper);
    this.kafkaTemplate = kafkaTemplate;
  }

  @KafkaListener(topics = "${kafka.topic.request}")
  public void processExerciseRequest(@Payload ExerciseResultFilter filter,
      @Header(KafkaHeaders.RECEIVED_KEY) Long chatId) {
    Pageable pageable = PageRequest.of(0, 20);
    Predicate predicate = predicateBuilder.apply(filter);
    List<ExerciseResultDto> exerciseResultDtoList =
        repository.findAll(predicate, pageable).stream().map(mapper::toDto).toList();
    PageDto<ExerciseResultDto> exerciseResultDtoPageDto = PageDto.<ExerciseResultDto>builder()
        .content(exerciseResultDtoList)
        .pageNumber(0)
        .pageSize(20)
        .totalElements(exerciseResultDtoList.size())
        .build();

    kafkaTemplate.send(exerciseResultTopicResponse, chatId, exerciseResultDtoPageDto);
    log.info("sent to kafka topic: {}, {}", exerciseResultTopicResponse, exerciseResultDtoPageDto);
  }
}
