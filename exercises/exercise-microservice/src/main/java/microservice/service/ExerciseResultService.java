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

  ExerciseMapper exerciseMapper;

  protected ExerciseResultService(ExerciseResultRepository repository, ExerciseResultMapper mapper) {
    super(repository, mapper);
  }

  public List<ExerciseResultDto> getExerciseResults(ExerciseDto exercise, String chatId) {
    return repository.findByExerciseAndUserChatIdOrderByDateDesc(exerciseMapper.toEntity(exercise), chatId)
        .stream()
        .map(mapper::toDto)
        .toList();
  }
}
