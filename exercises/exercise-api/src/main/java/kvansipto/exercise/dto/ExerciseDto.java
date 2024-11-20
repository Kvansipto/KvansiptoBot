package kvansipto.exercise.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ExerciseDto extends BaseDto {

  String name;
  String description;
  String videoUrl;
  String imageUrl;
  String muscleGroup;
}