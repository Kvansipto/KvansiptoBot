package io.project.kvansiptobot.model;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ExerciseResultRepository extends CrudRepository<ExerciseResult, Long> {

  List<ExerciseResult> findByExerciseOrderByDateDesc(Exercise exercise);
}
