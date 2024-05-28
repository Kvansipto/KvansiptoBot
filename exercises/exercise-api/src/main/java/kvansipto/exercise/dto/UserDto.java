package kvansipto.exercise.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.sql.Timestamp;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Valid
@Data
public class UserDto extends BaseDto {

  @NotNull
  Long chatId;
  String userName;
  String firstName;
  String lastName;
  Timestamp registeredAt;
}