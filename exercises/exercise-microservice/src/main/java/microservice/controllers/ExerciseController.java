package microservice.controllers;

import java.util.Arrays;
import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.resources.ExerciseApi;
import microservice.entity.MuscleGroup;
import microservice.service.ExerciseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExerciseController implements ExerciseApi {

//  private static final Logger log = LoggerFactory.getLogger(ExerciseController.class);

  @Autowired
  ExerciseService service;

  @Override
  @GetMapping("/exercises")
  public List<ExerciseDto> getExercisesByMuscleGroup(@RequestParam("muscleGroup") String muscleGroup) {
//    log.info("Получение упражнений для группы мышц: {}" + muscleGroup);
    System.out.println("Получение упражнений для группы мышц: " + muscleGroup);
    return service.getExercisesByMuscleGroup(muscleGroup);
  }

  @Override
  @GetMapping("/exercise")
  public ExerciseDto getExerciseByName(@RequestParam("name") String exerciseName) {
    return service.getExerciseByName(exerciseName);
  }

  @Override
  @GetMapping("/muscle-groups")
  public List<String> getMuscleGroups() {
    System.out.println("Получение списка групп мышц");
    return Arrays.stream(MuscleGroup.values()).map(MuscleGroup::getName).toList();
  }
}