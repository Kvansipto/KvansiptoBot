package kvansipto.exercise.filter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import kvansipto.exercise.dto.ExerciseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ExerciseResultFilter {

  @NotBlank
  String userChatId;
  ExerciseDto exerciseDto;
  @PastOrPresent
  LocalDate date;
}
