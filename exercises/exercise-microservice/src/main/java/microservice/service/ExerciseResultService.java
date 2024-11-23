package microservice.service;

import java.util.List;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.filter.ExerciseResultFilter;
import lombok.extern.slf4j.Slf4j;
import microservice.entity.ExerciseResult;
import microservice.mapper.ExerciseResultMapper;
import microservice.repository.ExerciseResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExerciseResultService extends
    BaseMappedService<ExerciseResult, ExerciseResultDto, Long, ExerciseResultRepository, ExerciseResultMapper> {

  @Autowired
  private ExerciseResultPredicateBuilder predicateBuilder;

  protected ExerciseResultService(ExerciseResultRepository repository, ExerciseResultMapper mapper) {
    super(repository, mapper);
  }

  public List<ExerciseResultDto> findExerciseResults(ExerciseResultFilter filter) {
    var pageable = PageRequest.of(0, 20);
    var predicate = predicateBuilder.apply(filter);
    return repository.findAll(predicate, pageable).stream().map(mapper::toDto).toList();
  }
}
