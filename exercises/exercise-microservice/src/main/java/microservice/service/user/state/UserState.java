package microservice.service.user.state;

import java.io.Serializable;
import java.time.LocalDate;
import kvansipto.exercise.dto.ExerciseDto;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Data
public class UserState implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long chatId;
  private ExerciseDto currentExercise;
  private UserStateType userStateType;
  private LocalDate exerciseResultDate;
}
