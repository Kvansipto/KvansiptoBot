package kvansipto.exercise.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@Valid
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseResultDto extends BaseDto {

  ExerciseDto exercise;
  double weight;
  int numberOfSets;
  int numberOfRepetitions;
  UserDto user;
  String comment;

  @PastOrPresent(message = "The date cannot be in the future")
  LocalDate date;
}