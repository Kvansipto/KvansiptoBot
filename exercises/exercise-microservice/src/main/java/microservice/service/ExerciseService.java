package microservice.service;

import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import microservice.entity.Exercise;
import microservice.entity.MuscleGroup;
import microservice.mapper.ExerciseMapper;
import microservice.repository.ExerciseRepository;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheNames = "exercises")
public class ExerciseService extends
    BaseMappedService<Exercise, ExerciseDto, Long, ExerciseRepository, ExerciseMapper> {

  protected ExerciseService(ExerciseRepository repository, ExerciseMapper exerciseMapper) {
    super(repository, exerciseMapper);
  }

  @Cacheable(key = "#muscleGroup")
  public List<ExerciseDto> getExercisesByMuscleGroup(String muscleGroup) {

    MuscleGroup existedMuscleGroup = MuscleGroup.fromName(muscleGroup);
    return repository.findByMuscleGroup(existedMuscleGroup).stream()
        .map(mapper::toDto)
        .toList();
  }

  @Cacheable(key = "#name")
  public ExerciseDto getExerciseByName(String name) {
    return mapper.toDto(repository.findByName(name));
  }
}
