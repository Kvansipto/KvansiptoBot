package kvansipto.exercise.dto;

import jakarta.validation.Valid;
import java.sql.Timestamp;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Valid
@Data
@SuperBuilder
public class UserDto extends BaseDto {

  String userName;
  String firstName;
  String lastName;
  Timestamp registeredAt;
}