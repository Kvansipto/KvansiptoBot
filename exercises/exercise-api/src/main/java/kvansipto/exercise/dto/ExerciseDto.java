package kvansipto.exercise.dto;

import java.io.Serializable;
import lombok.Data;
import lombok.Value;

@Data
public class ExerciseDto extends BaseDto {

//  long id;
  String name;
  String description;
  String videoUrl;
  String imageUrl;
}