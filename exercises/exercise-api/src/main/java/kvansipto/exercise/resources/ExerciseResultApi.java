package kvansipto.exercise.resources;

import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.filter.ExerciseResultFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExerciseResultApi {

  Page<ExerciseResultDto> getExerciseResults(ExerciseResultFilter filter, Pageable pageable);

  ExerciseResultDto saveExerciseResult(ExerciseResultDto exerciseResultDto);
}
