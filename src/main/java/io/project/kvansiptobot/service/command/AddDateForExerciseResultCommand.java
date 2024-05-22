package io.project.kvansiptobot.service.command;

import io.project.kvansiptobot.model.Exercise;
import io.project.kvansiptobot.repository.ExerciseRepository;
import io.project.kvansiptobot.service.UserState;
import io.project.kvansiptobot.service.UserStateFactory;
import io.project.kvansiptobot.service.UserStateService;
import io.project.kvansiptobot.service.wrapper.BotApiMethodInterface;
import io.project.kvansiptobot.service.wrapper.SendMessageWrapper;
import io.project.kvansiptobot.utils.KeyboardMarkupUtil;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class AddDateForExerciseResultCommand extends Command {

  @Autowired
  ExerciseRepository exerciseRepository;
  @Autowired
  UserStateService userStateService;
  @Autowired
  private UserStateFactory userStateFactory;

  public static final String ADD_DATE_EXERCISE_RESULT_TEXT = "ADD_DATE_EXERCISE_RESULT_";

  @Override
  public boolean supports(Update update) {
    return update.hasCallbackQuery() && update.getCallbackQuery().getData()
        .startsWith(ExerciseCommand.ADD_EXERCISE_RESULT_TEXT);
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    String exerciseName = update.getCallbackQuery().getData().split("_")[3];
    Long chatId = update.getCallbackQuery().getMessage().getChatId();
    Exercise exercise = exerciseRepository.findByName(exerciseName);

    UserState userState = userStateFactory.createUserSession(chatId);
    userState.setCurrentExercise(exercise);
    userState.setCurrentState("CHOOSING DATE");
    userStateService.setCurrentState(chatId, userState);

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");

    Map<String, String> dataToInlineKeyboardMarkup = new HashMap<>();

    for (int i = 0; i < DayOfWeek.values().length; i++) {
      String date = LocalDate.now().minusDays(i).format(dtf);
      if (i == 0) {
        dataToInlineKeyboardMarkup.put("Сегодня", ADD_DATE_EXERCISE_RESULT_TEXT + date);
      } else if (i == 1) {
        dataToInlineKeyboardMarkup.put("Вчера", ADD_DATE_EXERCISE_RESULT_TEXT + date);
      } else {
        dataToInlineKeyboardMarkup.put(date, ADD_DATE_EXERCISE_RESULT_TEXT + date);
      }
    }
    return SendMessageWrapper.newBuilder()
        .chatId(chatId)
        .replyMarkup(KeyboardMarkupUtil.generateInlineKeyboardMarkup(dataToInlineKeyboardMarkup,2))
        .text("Выберите дату")
        .build();
  }
}
