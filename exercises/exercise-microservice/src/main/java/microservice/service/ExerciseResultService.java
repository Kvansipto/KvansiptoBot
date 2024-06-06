package microservice.service;

import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.ExerciseResultDto;
import microservice.entity.ExerciseResult;
import microservice.mapper.ExerciseMapper;
import microservice.mapper.ExerciseResultMapper;
import microservice.repository.ExerciseResultRepository;
import org.springframework.stereotype.Service;

@Service
public class ExerciseResultService extends
    BaseMappedService<ExerciseResult, ExerciseResultDto, String, ExerciseResultRepository, ExerciseResultMapper> {

  private final ExerciseMapper exerciseMapper;

  protected ExerciseResultService(ExerciseResultRepository repository, ExerciseResultMapper mapper,
      ExerciseMapper exerciseMapper) {
    super(repository, mapper);
    this.exerciseMapper = exerciseMapper;
  }

  public List<ExerciseResultDto> getExerciseResults(ExerciseDto exercise, String chatId) {
    return repository.findAllByExerciseAndUserIdOrderByDateDesc(exerciseMapper.toEntity(exercise), chatId)
        .stream()
        .map(mapper::toDto)
        .toList();
  }
}
