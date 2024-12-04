package microservice.repository;

import java.util.List;
import microservice.entity.ExerciseResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface ExerciseResultRepository extends JpaRepository<ExerciseResult, Long>,
    QuerydslPredicateExecutor<ExerciseResult> {

  List<ExerciseResult> findAllByExercise_Id(Long id);
}
