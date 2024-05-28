package kvansipto.exercise.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = true)
@Data
@Valid
@Builder
public class ExerciseResultDto extends BaseDto {

  ExerciseDto exercise;
  double weight;
  byte numberOfSets;
  byte numberOfRepetitions;
  UserDto user;

  @PastOrPresent(message = "The date cannot be in the future")
  LocalDate date;
}