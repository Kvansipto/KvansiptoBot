package kvansipto.exercise.resources;

import java.util.List;
import kvansipto.exercise.dto.ExerciseResultDto;

public interface ExerciseApi {

  List<ExerciseResultDto> getResults();
}
