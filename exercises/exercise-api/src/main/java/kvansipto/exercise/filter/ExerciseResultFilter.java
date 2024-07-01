package kvansipto.exercise.filter;

import java.time.LocalDate;
import kvansipto.exercise.dto.ExerciseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ExerciseResultFilter {

  String userChatId;
  ExerciseDto exerciseDto;
  LocalDate date;
}
