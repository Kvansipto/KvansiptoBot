package microservice.controllers;

import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.resources.ExerciseResultApi;
import microservice.service.ExerciseResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExerciseResultController implements ExerciseResultApi {

  @Autowired
  ExerciseResultService service;

  @Override
  @PostMapping("/exercise-results/")
  public List<ExerciseResultDto> getExerciseResults(ExerciseDto exercise, String chatId) {
    return service.getExerciseResults(exercise, chatId);
  }

  @Override
  @PostMapping("/exercise-results")
  public ExerciseResultDto saveExerciseResult(ExerciseResultDto exerciseResultDto) {
    return service.create(exerciseResultDto);
  }
}
