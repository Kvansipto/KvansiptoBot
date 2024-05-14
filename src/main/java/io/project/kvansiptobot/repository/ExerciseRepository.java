package io.project.kvansiptobot.repository;

import java.util.List;

import io.project.kvansiptobot.model.Exercise;
import io.project.kvansiptobot.model.MuscleGroup;
import org.springframework.data.repository.CrudRepository;

public interface ExerciseRepository extends CrudRepository<Exercise, Long> {

  List<Exercise> findByMuscleGroup(MuscleGroup muscleGroup);
}
