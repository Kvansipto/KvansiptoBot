package kvansipto.exercise.resources;

import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;

public interface ExerciseApi {

  List<ExerciseDto> getExercisesByMuscleGroup(String muscleGroup);

  ExerciseDto getExerciseByName(String exerciseName);

  List<String> getMuscleGroups();
}
