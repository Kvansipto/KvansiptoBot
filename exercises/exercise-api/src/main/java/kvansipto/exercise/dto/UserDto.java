package kvansipto.exercise.dto;

import jakarta.validation.Valid;
import java.sql.Timestamp;

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
public class UserDto extends BaseDto {

  String userName;
  String firstName;
  String lastName;
  Timestamp registeredAt;
}