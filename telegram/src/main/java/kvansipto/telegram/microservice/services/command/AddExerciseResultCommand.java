package kvansipto.telegram.microservice.services.command;

import java.time.LocalDate;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.telegram.microservice.services.UserState;
import kvansipto.telegram.microservice.services.UserStateFactory;
import kvansipto.telegram.microservice.services.UserStateService;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.SendMessageWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class AddExerciseResultCommand extends Command {

  @Autowired
  UserStateService userStateService;
  @Autowired
  UserStateFactory userStateFactory;

  public static final String ADD_EXERCISE_RESULT_TEXT = "Введите результат в формате:\n[(Вес в кг) (количество "
      + "подходов) (количество повторений)]\n\n" + "Пример сообщения: 12.5 8 15";
  public static final String WAITING_FOR_RESULT_STATE_TEXT = "WAITING_FOR_RESULT";

  @Override
  public boolean supports(Update update) {
    return update.hasCallbackQuery() && update.getCallbackQuery().getData()
        .startsWith(AddDateForExerciseResultCommand.ADD_DATE_EXERCISE_RESULT_TEXT);
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    String date = update.getCallbackQuery().getData().split("_")[4];

    String chatId = update.getCallbackQuery().getMessage().getChatId().toString();

    int days = Integer.parseInt(date.split("/")[0]);
    int month = Integer.parseInt(date.split("/")[1]);
    LocalDate localDate = LocalDate.of(LocalDate.now().getYear(), month, days);
    ExerciseDto exercise = userStateService.getCurrentState(chatId).getCurrentExercise();

    UserState userState = userStateFactory.createUserSession(chatId);
    userState.setCurrentExercise(exercise);
    userState.setExerciseResultDate(localDate);
    userState.setCurrentState(WAITING_FOR_RESULT_STATE_TEXT);
    userStateService.setCurrentState(chatId, userState);
    return SendMessageWrapper.newBuilder()
        .chatId(chatId)
        .text(ADD_EXERCISE_RESULT_TEXT)
        .build();
  }
}
