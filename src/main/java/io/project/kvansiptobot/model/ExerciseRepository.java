package io.project.kvansiptobot.model;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ExerciseRepository extends CrudRepository<Exercise, Long> {

  List<Exercise> findByMuscleGroup(MuscleGroup muscleGroup);
}