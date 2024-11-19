package kvansipto.telegram.microservice.services.command;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.UserStateFactory;
import kvansipto.telegram.microservice.services.UserStateService;
import kvansipto.telegram.microservice.services.dto.AnswerData;
import kvansipto.telegram.microservice.services.dto.AnswerDto;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.EditMessageWrapper;
import kvansipto.telegram.microservice.utils.KeyboardMarkupUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class AddDateForExerciseResultCommand extends Command {

  private final UserStateService userStateService;
  private final UserStateFactory userStateFactory;
  private final RestToExercises restToExercises;

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
    var exerciseName = AnswerData.deserialize(update.getCallbackQuery().getData()).getHiddenText();
    var chatId = update.getCallbackQuery().getMessage().getChatId();
    var exercise = restToExercises.getExerciseByName(exerciseName);

    var userState = userStateFactory.createUserSession(chatId);
    userState.setCurrentExercise(exercise);
    userState.setCurrentState("CHOOSING DATE");
    userStateService.setCurrentState(chatId, userState);

    var dtf = DateTimeFormatter.ofPattern("dd/MM");
    List<AnswerDto> answers = new ArrayList<>();

    String[] dayTexts = {TODAY_TEXT, YESTERDAY_TEXT};
    for (int i = 0; i < DayOfWeek.values().length; i++) {
      var date = LocalDate.now().minusDays(i).format(dtf);
      var text = i < dayTexts.length ? dayTexts[i] : date;
      answers.add(new AnswerDto(text, ADD_DATE_EXERCISE_RESULT_TEXT, date));
    }
    return EditMessageWrapper.newBuilder()
        .chatId(chatId)
        .messageId(update.getCallbackQuery().getMessage().getMessageId())
        .replyMarkup(KeyboardMarkupUtil.createRows(answers, 2))
        .text(ADD_DATE_FOR_EXERCISE_RESULT_TEXT)
        .build();
  }
}
