package microservice.service.command;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import kvansipto.exercise.wrapper.EditMessageWrapper;
import microservice.service.dto.AnswerData;
import microservice.service.event.UserInputCommandEvent;
import microservice.service.user.state.UserState;
import microservice.service.user.state.UserStateService;
import microservice.service.user.state.UserStateType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddExerciseResultCommand extends Command {

  @Autowired
  private UserStateService userStateService;

  private static final String ADD_EXERCISE_RESULT_TEXT =
      "Введите результат в формате:\n[(Вес в кг) (количество подходов) (количество повторений)]\n\nПример сообщения: 12.5 8 15";

  @Override
  public boolean supports(UserInputCommandEvent event) {
    var chatId = event.chatId();
    var buttonCode = AnswerData.deserialize(event.update().getMessage()).getButtonCode();

    var userState = userStateService.getCurrentState(chatId).orElse(null);

    return userState != null
        && UserStateType.CHOOSING_DATE.equals(userState.getUserStateType())
        && AddDateForExerciseResultCommand.ADD_DATE_EXERCISE_RESULT_TEXT.equals(buttonCode);
  }

  @Override
  public void process(UserInputCommandEvent event) {
    var chatId = event.chatId();
    var date = AnswerData.deserialize(event.update().getMessage()).getButtonText();
    LocalDate localDate;
    var formatter = DateTimeFormatter.ofPattern("dd/MM");
    localDate = switch (date) {
      case "Сегодня" -> LocalDate.now();
      case "Вчера" -> LocalDate.now().minusDays(1);
      default -> MonthDay.parse(date, formatter).atYear(LocalDate.now().getYear());
    };

    var userState = userStateService.getCurrentState(chatId).orElse(new UserState());
    userState.setExerciseResultDate(localDate);
    userState.setUserStateType(UserStateType.WAITING_FOR_RESULT);
    userStateService.setCurrentState(chatId, userState);

    kafkaExerciseService.sendBotApiMethod(event.chatId(),
            EditMessageWrapper.newBuilder()
                .chatId(chatId)
                .messageId(event.update().getMessageId())
                .text(ADD_EXERCISE_RESULT_TEXT)
                .build())
        .subscribe();
  }
}
