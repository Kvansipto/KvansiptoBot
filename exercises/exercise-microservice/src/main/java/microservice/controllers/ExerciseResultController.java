package microservice.controllers;

import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.filter.ExerciseResultFilter;
import kvansipto.exercise.resources.ExerciseResultApi;
import microservice.service.ExerciseResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExerciseResultController implements ExerciseResultApi {

  @Autowired
  ExerciseResultService service;

  @Override
  @PostMapping("/exercise-results/")
  public Page<ExerciseResultDto> getExerciseResults(@RequestBody ExerciseResultFilter filter,
      @PageableDefault(size = 20) Pageable pageable) {
    return service.getExerciseResults(filter, pageable);
  }

  @Override
  @PostMapping("/exercise-results")
  public ExerciseResultDto saveExerciseResult(@RequestBody ExerciseResultDto exerciseResultDto) {
    return service.create(exerciseResultDto);
  }
}
