package microservice.service.command;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import kvansipto.exercise.wrapper.EditMessageWrapper;
import lombok.RequiredArgsConstructor;
import microservice.service.KeyboardMarkupUtil;
import microservice.service.dto.AnswerData;
import microservice.service.dto.AnswerDto;
import microservice.service.event.UserInputCommandEvent;
import microservice.service.user.state.UserState;
import microservice.service.user.state.UserStateService;
import microservice.service.user.state.UserStateType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddDateForExerciseResultCommand extends Command {

  private final UserStateService userStateService;

  public static final String ADD_DATE_FOR_EXERCISE_RESULT_TEXT = "Выберите дату";
  public static final String TODAY_TEXT = "Сегодня";
  public static final String YESTERDAY_TEXT = "Вчера";
  public static final String ADD_DATE_EXERCISE_RESULT_TEXT = "ADD_DATE_EXERCISE_RESULT";

  @Override
  public boolean supports(UserInputCommandEvent event) {
    var chatId = event.chatId();
    var buttonCode = AnswerData.deserialize(event.update().getMessage()).getButtonCode();
    var userState = userStateService.getCurrentState(chatId).orElse(null);
    return userState != null
        && UserStateType.VIEWING_EXERCISE.equals(userState.getUserStateType())
        && ExerciseCommand.ADD_EXERCISE_RESULT_TEXT.equals(buttonCode);
  }

  @Override
  public void process(UserInputCommandEvent event) {
    var chatId = event.chatId();

    var userState = userStateService.getCurrentState(chatId).orElse(new UserState());
    userState.setUserStateType(UserStateType.CHOOSING_DATE);
    userStateService.setCurrentState(chatId, userState);

    var dtf = DateTimeFormatter.ofPattern("dd/MM");
    List<AnswerDto> answers = new ArrayList<>();

    String[] dayTexts = {TODAY_TEXT, YESTERDAY_TEXT};
    for (int i = 0; i < DayOfWeek.values().length; i++) {
      var date = LocalDate.now().minusDays(i).format(dtf);
      var text = i < dayTexts.length ? dayTexts[i] : date;
      answers.add(new AnswerDto(text, ADD_DATE_EXERCISE_RESULT_TEXT));
    }
    kafkaExerciseService.sendBotApiMethod(event.chatId(),
            EditMessageWrapper.newBuilder()
                .chatId(chatId)
                .messageId(event.update().getMessageId())
                .replyMarkup(KeyboardMarkupUtil.createRows(answers, 2))
                .text(ADD_DATE_FOR_EXERCISE_RESULT_TEXT)
                .build())
        .subscribe();
  }
}
