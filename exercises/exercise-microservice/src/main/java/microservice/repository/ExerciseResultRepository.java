package microservice.repository;

import io.project.kvansiptobot.model.Exercise;
import io.project.kvansiptobot.model.ExerciseResult;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ExerciseResultRepository extends CrudRepository<ExerciseResult, Long> {

  @Query("SELECT er FROM exercise_result er WHERE er.exercise = :exercise AND er.user.chatId = :userChatId ORDER BY "
      + "er.date DESC")
  List<ExerciseResult> findByExerciseAndUserChatIdOrderByDateDesc(@Param("exercise") Exercise exercise,
      @Param("userChatId") Long userChatId);
}
