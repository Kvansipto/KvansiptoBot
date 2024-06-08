package kvansipto.exercise.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public abstract class BaseDto {

  private String id;
}
