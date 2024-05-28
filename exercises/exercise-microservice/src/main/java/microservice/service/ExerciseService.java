package microservice.service;

import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.MuscleGroupDto;
import microservice.entity.Exercise;
import microservice.mapper.ExerciseMapper;
import microservice.mapper.MuscleGroupMapper;
import microservice.repository.ExerciseRepository;
import org.springframework.stereotype.Service;

@Service
public class ExerciseService extends
    BaseMappedService<Exercise, ExerciseDto, String, ExerciseRepository, ExerciseMapper> {

  MuscleGroupMapper muscleGroupMapper;

  protected ExerciseService(ExerciseRepository repository, ExerciseMapper mapper) {
    super(repository, mapper);
  }

  public List<ExerciseDto> getExercisesByMuscleGroup(MuscleGroupDto muscleGroup) {
    return repository.findByMuscleGroup(muscleGroupMapper.toEntity(muscleGroup)).stream()
        .map(mapper::toDto)
        .toList();
  }

  public ExerciseDto getExerciseByName(String name) {
    return mapper.toDto(repository.findByName(name));
  }
}
