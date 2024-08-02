package microservice.service;

import com.querydsl.core.types.Predicate;
import java.util.List;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.filter.ExerciseResultFilter;
import microservice.entity.ExerciseResult;
import microservice.mapper.ExerciseMapper;
import microservice.mapper.ExerciseResultMapper;
import microservice.repository.ExerciseResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ExerciseResultService extends
    BaseMappedService<ExerciseResult, ExerciseResultDto, Long, ExerciseResultRepository, ExerciseResultMapper> {

  private final ExerciseMapper exerciseMapper;

  @Autowired
  private ExerciseResultPredicateBuilder predicateBuilder;

  protected ExerciseResultService(ExerciseResultRepository repository, ExerciseResultMapper mapper,
      ExerciseMapper exerciseMapper) {
    super(repository, mapper);
    this.exerciseMapper = exerciseMapper;
  }

  public Page<ExerciseResultDto> getExerciseResults(ExerciseResultFilter filter, Pageable pageable) {
    Predicate predicate = predicateBuilder.apply(filter);
    List<ExerciseResultDto> exerciseResultDtoList =
        repository.findAll(predicate, pageable).stream().map(mapper::toDto).toList();

    return new PageImpl<>(exerciseResultDtoList, pageable, exerciseResultDtoList.size());
  }
}
