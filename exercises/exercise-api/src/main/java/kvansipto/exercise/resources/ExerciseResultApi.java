package kvansipto.exercise.resources;

import kvansipto.exercise.dto.ExerciseResultDto;

public interface ExerciseResultApi {

//  Page<ExerciseResultDto> getExerciseResults(ExerciseResultFilter filter, Pageable pageable);

  ExerciseResultDto saveExerciseResult(ExerciseResultDto exerciseResultDto);
}
