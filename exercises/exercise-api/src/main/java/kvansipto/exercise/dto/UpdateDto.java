package kvansipto.exercise.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Valid
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDto extends BaseDto {

  String message;
  Integer messageId;
  UserDto user;
}