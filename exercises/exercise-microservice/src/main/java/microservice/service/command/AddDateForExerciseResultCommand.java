package microservice.service.command;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.wrapper.EditMessageWrapper;
import microservice.service.KeyboardMarkupUtil;
import microservice.service.UserState;
import microservice.service.UserStateService;
import microservice.service.dto.AnswerData;
import microservice.service.dto.AnswerDto;
import microservice.service.event.UserInputCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddDateForExerciseResultCommand extends Command {

  @Autowired
  private UserStateService userStateService;

  public static final String ADD_DATE_FOR_EXERCISE_RESULT_TEXT = "Выберите дату";
  public static final String TODAY_TEXT = "Сегодня";
  public static final String YESTERDAY_TEXT = "Вчера";
  public static final String ADD_DATE_EXERCISE_RESULT_TEXT = "ADD_DATE_EXERCISE_RESULT";

  @Override
  public boolean supports(UserInputCommandEvent event) {
    Long chatId = event.chatId();
    String buttonCode = AnswerData.deserialize(event.update().getMessage()).getButtonCode();
    UserState userState = userStateService.getCurrentState(chatId).orElse(null);
    return userState != null
        && "CHOOSING EXERCISE".equals(userState.getCurrentState())
        && ExerciseCommand.ADD_EXERCISE_RESULT_TEXT.equals(buttonCode);
  }

  @Override
  public void process(UserInputCommandEvent event) {
    Long chatId = event.chatId();

    UserState userState = userStateService.getCurrentState(chatId).orElse(new UserState());
    ExerciseDto exercise = userState.getCurrentExercise();

    userState.setCurrentState("CHOOSING DATE");
    userStateService.setCurrentState(chatId, userState);

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");
    List<AnswerDto> answers = new ArrayList<>();

    String[] dayTexts = {TODAY_TEXT, YESTERDAY_TEXT};
    for (int i = 0; i < DayOfWeek.values().length; i++) {
      String date = LocalDate.now().minusDays(i).format(dtf);
      String text = i < dayTexts.length ? dayTexts[i] : date;
      answers.add(new AnswerDto(text, ADD_DATE_EXERCISE_RESULT_TEXT));
    }

    kafkaTemplate.send("actions-from-exercises", event.chatId(),
        EditMessageWrapper.newBuilder()
            .chatId(chatId)
            .messageId(event.update().getMessageId())
            .replyMarkup(KeyboardMarkupUtil.createRows(answers, 2))
            .text(ADD_DATE_FOR_EXERCISE_RESULT_TEXT)
            .build());
  }
}
