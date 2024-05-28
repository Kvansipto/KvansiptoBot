package kvansipto.telegram.microservice.services.command;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.UserState;
import kvansipto.telegram.microservice.services.UserStateFactory;
import kvansipto.telegram.microservice.services.UserStateService;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.SendMessageWrapper;
import kvansipto.telegram.microservice.utils.KeyboardMarkupUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class AddDateForExerciseResultCommand extends Command {

  @Autowired
  UserStateService userStateService;
  @Autowired
  private UserStateFactory userStateFactory;
  @Autowired
  private RestToExercises restToExercises;

  public static final String ADD_DATE_FOR_EXERCISE_RESULT_TEXT = "Выберите дату";
  public static final String TODAY_TEXT = "Сегодня";
  public static final String YESTERDAY_TEXT = "Вчера";
  public static final String ADD_DATE_EXERCISE_RESULT_TEXT = "ADD_DATE_EXERCISE_RESULT_";

  @Override
  public boolean supports(Update update) {
    return update.hasCallbackQuery() && update.getCallbackQuery().getData()
        .startsWith(ExerciseCommand.ADD_EXERCISE_RESULT_TEXT);
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    String exerciseName = update.getCallbackQuery().getData().split("_")[3];
    String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
//    ExerciseDto exercise = exerciseRepository.findByName(exerciseName);
    ExerciseDto exercise = restToExercises.getExerciseByName(exerciseName);

    UserState userState = userStateFactory.createUserSession(chatId);
    userState.setCurrentExercise(exercise);
    userState.setCurrentState("CHOOSING DATE");
    userStateService.setCurrentState(chatId, userState);

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");

    Map<String, String> dataToInlineKeyboardMarkup = new HashMap<>();

    for (int i = 0; i < DayOfWeek.values().length; i++) {
      String date = LocalDate.now().minusDays(i).format(dtf);
      if (i == 0) {
        dataToInlineKeyboardMarkup.put(TODAY_TEXT, ADD_DATE_EXERCISE_RESULT_TEXT + date);
      } else if (i == 1) {
        dataToInlineKeyboardMarkup.put(YESTERDAY_TEXT, ADD_DATE_EXERCISE_RESULT_TEXT + date);
      } else {
        dataToInlineKeyboardMarkup.put(date, ADD_DATE_EXERCISE_RESULT_TEXT + date);
      }
    }
    return SendMessageWrapper.newBuilder()
        .chatId(chatId)
        .replyMarkup(KeyboardMarkupUtil.generateInlineKeyboardMarkup(dataToInlineKeyboardMarkup,2))
        .text(ADD_DATE_FOR_EXERCISE_RESULT_TEXT)
        .build();
  }
}
