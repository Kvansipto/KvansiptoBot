package microservice.service.command;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.wrapper.EditMessageWrapper;
import microservice.service.UserState;
import microservice.service.UserStateService;
import microservice.service.dto.AnswerData;
import microservice.service.event.UserInputCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddExerciseResultCommand extends Command {

  @Autowired
  private UserStateService userStateService;

  public static final String ADD_EXERCISE_RESULT_TEXT =
      "Введите результат в формате:\n[(Вес в кг) (количество подходов) (количество повторений)]\n\n"
          + "Пример сообщения: 12.5 8 15";
  public static final String WAITING_FOR_RESULT_STATE_TEXT = "WAITING_FOR_RESULT";

  @Override
  public boolean supports(UserInputCommandEvent event) {
    Long chatId = event.chatId();
    String buttonCode = AnswerData.deserialize(event.update().getMessage()).getButtonCode();

    UserState userState = userStateService.getCurrentState(chatId).orElse(null);

    return userState != null
        && "CHOOSING DATE".equals(userState.getCurrentState())
        && AddDateForExerciseResultCommand.ADD_DATE_EXERCISE_RESULT_TEXT.equals(buttonCode);
  }

  @Override
  public void process(UserInputCommandEvent event) {
    Long chatId = event.chatId();
    String date = AnswerData.deserialize(event.update().getMessage()).getButtonText();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
    LocalDate localDate = LocalDate.parse(date, formatter);

    UserState userState = userStateService.getCurrentState(chatId).orElse(new UserState());
    ExerciseDto exercise = userState.getCurrentExercise();

    userState.setExerciseResultDate(localDate);
    userState.setCurrentState(WAITING_FOR_RESULT_STATE_TEXT);
    userStateService.setCurrentState(chatId, userState);

    kafkaTemplate.send("actions-from-exercises", event.chatId(),
        EditMessageWrapper.newBuilder()
            .chatId(chatId)
            .messageId(event.update().getMessageId())
            .text(ADD_EXERCISE_RESULT_TEXT)
            .build());
  }
}
