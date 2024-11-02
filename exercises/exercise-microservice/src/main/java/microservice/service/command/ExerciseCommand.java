package microservice.service.command;

import java.util.ArrayList;
import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.wrapper.EditMessageWrapper;
import microservice.service.ExerciseService;
import microservice.service.KeyboardMarkupUtil;
import microservice.service.dto.AnswerData;
import microservice.service.dto.AnswerDto;
import microservice.service.event.UserInputCommandEvent;
import microservice.service.user.state.UserState;
import microservice.service.user.state.UserStateService;
import microservice.service.user.state.UserStateType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExerciseCommand extends Command {

  @Autowired
  private ExerciseService exerciseService;

  @Autowired
  private UserStateService userStateService;

  public static final String SHOW_EXERCISE_RESULT_HISTORY_BUTTON_TEXT = "SHOW_RESULT_HISTORY";
  public static final String ADD_EXERCISE_RESULT_BUTTON_TEXT = "ADD_RESULT";
  public static final String EXERCISE_TEXT = "Посмотрите видео с упражнением на YouTube: [Смотреть видео](%s)";
  public static final String SHOW_EXERCISE_RESULT_HISTORY = "E_HISTORY";
  public static final String ADD_EXERCISE_RESULT_TEXT = "ADD_RESULT";

  @Override
  public boolean supports(UserInputCommandEvent event) {
    Long chatId = event.chatId();
    String buttonCode = AnswerData.deserialize(event.update().getMessage()).getButtonCode();

    UserState userState = userStateService.getCurrentState(chatId).orElse(null);
    return userState != null
        && UserStateType.CHOOSING_EXERCISE.equals(userState.getUserStateType())
        && "exercise".equals(buttonCode);
  }

  @Override
  public void process(UserInputCommandEvent event) {
    Long chatId = event.chatId();

    String exerciseName = AnswerData.deserialize(event.update().getMessage()).getButtonText();
    ExerciseDto exercise = exerciseService.getExerciseByName(exerciseName);

    List<AnswerDto> answerDtoList = new ArrayList<>();
    answerDtoList.add(new AnswerDto(SHOW_EXERCISE_RESULT_HISTORY_BUTTON_TEXT, SHOW_EXERCISE_RESULT_HISTORY));
    answerDtoList.add(new AnswerDto(ADD_EXERCISE_RESULT_BUTTON_TEXT, ADD_EXERCISE_RESULT_TEXT));

    UserState userState = userStateService.getCurrentState(chatId).orElse(new UserState());
    userState.setUserStateType(UserStateType.VIEWING_EXERCISE);
    userState.setCurrentExercise(exercise);
    userStateService.setCurrentState(chatId, userState);

    kafkaService.send("actions-from-exercises", event.chatId(),
        EditMessageWrapper.newBuilder()
            .chatId(chatId)
            .messageId(event.update().getMessageId())
            .replyMarkup(KeyboardMarkupUtil.createRows(answerDtoList, 1))
            .text(String.format("%s%n" + EXERCISE_TEXT, exercise.getDescription(), exercise.getVideoUrl()))
            .parseMode("MarkdownV2")
            .disableWebPagePreview(false)
            .build(), kafkaTemplate);
  }
}
