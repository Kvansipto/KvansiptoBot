package kvansipto.exercise.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@Valid
@SuperBuilder
public class ExerciseResultDto extends BaseDto {

  ExerciseDto exercise;
  double weight;
  byte numberOfSets;
  byte numberOfRepetitions;
  UserDto user;

  @PastOrPresent(message = "The date cannot be in the future")
  LocalDate date;
}