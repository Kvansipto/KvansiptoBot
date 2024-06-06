package microservice.controllers;

import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.resources.ExerciseResultApi;
import microservice.service.ExerciseResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExerciseResultController implements ExerciseResultApi {

  @Autowired
  ExerciseResultService service;

  @Override
  @PostMapping("/exercise-results/")
  public List<ExerciseResultDto> getExerciseResults(@RequestBody Pair<ExerciseDto, String> pair) {
    return service.getExerciseResults(pair.getFirst(), pair.getSecond());
  }

  @Override
  @PostMapping("/exercise-results")
  public ExerciseResultDto saveExerciseResult(@RequestBody ExerciseResultDto exerciseResultDto) {
    return service.create(exerciseResultDto);
  }
}
