package kvansipto.exercise.resources;

import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.ExerciseResultDto;
import org.springframework.data.util.Pair;

public interface ExerciseResultApi {

  List<ExerciseResultDto> getExerciseResults(Pair<ExerciseDto, String> pair);

  ExerciseResultDto saveExerciseResult(ExerciseResultDto exerciseResultDto);
}
