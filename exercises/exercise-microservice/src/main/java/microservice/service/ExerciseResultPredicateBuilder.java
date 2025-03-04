package microservice.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import kvansipto.exercise.filter.ExerciseResultFilter;
import microservice.entity.MuscleGroup;
import microservice.entity.QExerciseResult;
import org.springframework.stereotype.Component;

@Component
public class ExerciseResultPredicateBuilder {

  private final QExerciseResult root = QExerciseResult.exerciseResult;

  public Predicate apply(ExerciseResultFilter search) {
    var builder = new BooleanBuilder();

    if (search.getUserChatId() != null) {
      builder.and(root.user.id.eq(search.getUserChatId()));
    }
    if (search.getExerciseDto() != null) {
      if (search.getExerciseDto().getName() != null) {
        builder.and(root.exercise.name.eq(search.getExerciseDto().getName()));
      }
      if (search.getExerciseDto().getId() != null) {
        builder.and(root.exercise.id.eq(search.getExerciseDto().getId()));
      }
      if (search.getExerciseDto().getMuscleGroup() != null) {
        builder.and(root.exercise.muscleGroup.eq(MuscleGroup.valueOf(search.getExerciseDto().getMuscleGroup().toUpperCase())));
      }
    }
    if (search.getDate() != null) {
      builder.and(root.date.eq(search.getDate()));
    }
    return builder;
  }
}
