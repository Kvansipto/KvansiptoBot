package kvansipto.telegram.microservice.services;

import java.time.LocalDate;
import kvansipto.exercise.dto.ExerciseDto;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Data
public class UserState {

  private String chatId;
  private ExerciseDto currentExercise;
  private String currentState;
  private LocalDate exerciseResultDate;
}
