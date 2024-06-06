package microservice.repository;

import java.util.List;
import microservice.entity.Exercise;
import microservice.entity.ExerciseResult;
import org.springframework.data.repository.CrudRepository;

public interface ExerciseResultRepository extends CrudRepository<ExerciseResult, String> {

  List<ExerciseResult> findAllByExerciseAndUserIdOrderByDateDesc(Exercise exercise, String userId);
}
