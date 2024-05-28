package microservice.controllers;

import java.util.Arrays;
import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.MuscleGroupDto;
import kvansipto.exercise.resources.ExerciseApi;
import microservice.entity.MuscleGroup;
import microservice.mapper.MuscleGroupMapper;
import microservice.service.ExerciseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExerciseController implements ExerciseApi {

  ExerciseService service;
  MuscleGroupMapper muscleGroupMapper;

  @Override
  @GetMapping("/exercises?muscle-group={muscleGroup}")
  public List<ExerciseDto> getExercisesByMuscleGroup(@PathVariable("muscleGroup") MuscleGroupDto muscleGroup) {
    return service.getExercisesByMuscleGroup(muscleGroup);
  }

  @Override
  @GetMapping("/exercises?name={exerciseName}")
  public ExerciseDto getExerciseByName(@PathVariable("exerciseName") String exerciseName) {
    return service.getExerciseByName(exerciseName);
  }

  @Override
  @GetMapping("/muscle-groups")
  public List<MuscleGroupDto> getMuscleGroups() {
    return Arrays.stream(MuscleGroup.values()).map(muscleGroupMapper::toDto).toList();
  }
}