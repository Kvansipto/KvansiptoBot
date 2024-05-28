package kvansipto.exercise.resources;

import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.dto.MuscleGroupDto;

public interface ExerciseApi {

  List<ExerciseDto> getExercisesByMuscleGroup(MuscleGroupDto muscleGroup);

  ExerciseDto getExerciseByName(String exerciseName);

  List<MuscleGroupDto> getMuscleGroups();
}
