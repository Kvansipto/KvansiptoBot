package kvansipto.exercise.resources;

import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.ExerciseResultDto;

public interface ExerciseResultApi {

  List<ExerciseResultDto> getExerciseResults(ExerciseDto exercise, String chatId);

  ExerciseResultDto saveExerciseResult(ExerciseResultDto exerciseResultDto);
}
