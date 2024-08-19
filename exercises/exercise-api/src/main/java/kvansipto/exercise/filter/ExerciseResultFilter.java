package kvansipto.exercise.filter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.io.Serializable;
import java.time.LocalDate;
import kvansipto.exercise.dto.ExerciseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseResultFilter implements Serializable {

  @NotNull
  Long userChatId;
  ExerciseDto exerciseDto;
  @PastOrPresent
  LocalDate date;
}
