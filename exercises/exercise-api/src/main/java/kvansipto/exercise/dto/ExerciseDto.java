package kvansipto.exercise.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class ExerciseDto extends BaseDto {

  String name;
  String description;
  String videoUrl;
  String imageUrl;
  String muscleGroup;
}