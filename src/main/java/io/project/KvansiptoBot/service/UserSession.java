package io.project.KvansiptoBot.service;

import io.project.KvansiptoBot.model.Exercise;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Data
public class UserSession {

  private Long chatId;
  private Exercise currentExercise;
  private Boolean isWaitingForResult;
}
