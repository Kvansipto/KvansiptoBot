package kvansipto.exercise.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;
import lombok.Value;

@Data
@Valid
public class ExerciseResultDto extends BaseDto {

//  long id;
  ExerciseDto exercise;
  double weight;
  byte numberOfSets;
  byte numberOfRepetitions;

  @PastOrPresent(message = "The date cannot be in the future")
  LocalDate date;
}