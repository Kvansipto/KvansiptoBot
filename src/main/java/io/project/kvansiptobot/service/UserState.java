package io.project.kvansiptobot.service;

import io.project.kvansiptobot.model.Exercise;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Data
public class UserState {

  private Long chatId;
  private Exercise currentExercise;
  private String currentState;
  private LocalDate exerciseResultDate;
}
