package kvansipto.telegram.microservice.services.command;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.UserState;
import kvansipto.telegram.microservice.services.UserStateFactory;
import kvansipto.telegram.microservice.services.UserStateService;
import kvansipto.telegram.microservice.services.dto.AnswerData;
import kvansipto.telegram.microservice.services.dto.AnswerDto;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.EditMessageWrapper;
import kvansipto.telegram.microservice.utils.KeyboardMarkupUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
    return update.hasCallbackQuery() && AnswerData.deserialize(update.getCallbackQuery().getData()).getButtonCode()
        .equals(ExerciseCommand.ADD_EXERCISE_RESULT_TEXT);
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    String exerciseName = AnswerData.deserialize(update.getCallbackQuery().getData()).getHiddenText();
    String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
    ExerciseDto exercise = restToExercises.getExerciseByName(exerciseName);

    UserState userState = userStateFactory.createUserSession(chatId);
    userState.setCurrentExercise(exercise);
    userState.setCurrentState("CHOOSING DATE");
    userStateService.setCurrentState(chatId, userState);

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");

    List<AnswerDto> answers = new ArrayList<>();

    for (int i = 0; i < DayOfWeek.values().length; i++) {
      String date = LocalDate.now().minusDays(i).format(dtf);
      if (i == 0) {
        answers.add(new AnswerDto(TODAY_TEXT, ADD_DATE_EXERCISE_RESULT_TEXT, date));
      } else if (i == 1) {
        answers.add(new AnswerDto(YESTERDAY_TEXT, ADD_DATE_EXERCISE_RESULT_TEXT, date));
      } else {
        answers.add(new AnswerDto(date, ADD_DATE_EXERCISE_RESULT_TEXT, date));
      }
    }
    return EditMessageWrapper.newBuilder()
        .chatId(chatId)
        .messageId(update.getCallbackQuery().getMessage().getMessageId())
        .replyMarkup(KeyboardMarkupUtil.createRows(answers, 2))
        .text(ADD_DATE_FOR_EXERCISE_RESULT_TEXT)
        .build();
  }
}
