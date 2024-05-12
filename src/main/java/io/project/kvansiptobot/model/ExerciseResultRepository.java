package io.project.kvansiptobot.model;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ExerciseResultRepository extends CrudRepository<ExerciseResult, Long> {

  List<ExerciseResult> findByExerciseOrderByDateDesc(Exercise exercise);

//  @Query("select e from exercise_result e where exercise = : exercise and user = : user order by date desc")
//  List<ExerciseResult> findByExerciseAndUserOrderByDateDesc(@Param("exercise") Exercise exercise, @Param(
//      "user") User user);

}
