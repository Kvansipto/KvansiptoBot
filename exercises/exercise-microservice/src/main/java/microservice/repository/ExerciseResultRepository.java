package microservice.repository;

import java.util.List;
import microservice.entity.Exercise;
import microservice.entity.ExerciseResult;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ExerciseResultRepository extends CrudRepository<ExerciseResult, String> {

  @Query("SELECT er FROM exercise_result er WHERE er.exercise = :exercise AND er.user.id = :userChatId ORDER BY "
      + "er.date DESC")
  List<ExerciseResult> findByExerciseAndUserChatIdOrderByDateDesc(@Param("exercise") Exercise exercise,
      @Param("userChatId") String userChatId);
}
