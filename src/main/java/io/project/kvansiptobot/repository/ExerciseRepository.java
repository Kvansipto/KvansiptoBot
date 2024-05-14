package io.project.kvansiptobot.repository;

import io.project.kvansiptobot.model.Exercise;
import io.project.kvansiptobot.model.MuscleGroup;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ExerciseRepository extends CrudRepository<Exercise, Long> {

  List<Exercise> findByMuscleGroup(MuscleGroup muscleGroup);

  Exercise findByName(String name);

  boolean existsByName(String name);
}
