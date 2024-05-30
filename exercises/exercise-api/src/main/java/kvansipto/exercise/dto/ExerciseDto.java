package kvansipto.exercise.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ExerciseDto extends BaseDto {

  String name;
  String description;
  String videoUrl;
  String imageUrl;
  String muscleGroup;
}