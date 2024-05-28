package kvansipto.exercise.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MuscleGroupDto extends BaseDto {

  String name;
}