package microservice.repository;

import microservice.entity.ExerciseResult;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;

public interface ExerciseResultRepository extends CrudRepository<ExerciseResult, String>,
    QuerydslPredicateExecutor<ExerciseResult> {
}
