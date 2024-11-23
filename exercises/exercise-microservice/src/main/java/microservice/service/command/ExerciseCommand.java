package microservice.service.command;

import java.util.ArrayList;
import java.util.List;
import kvansipto.exercise.wrapper.BotApiMethodInterface;
import kvansipto.exercise.wrapper.EditMessageWrapper;
import microservice.service.ExerciseService;
import microservice.service.KafkaExerciseService;
import microservice.service.KeyboardMarkupUtil;
import microservice.service.dto.AnswerData;
import microservice.service.dto.AnswerDto;
import microservice.service.event.UserInputCommandEvent;
import microservice.service.user.state.UserState;
import microservice.service.user.state.UserStateService;
import microservice.service.user.state.UserStateType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ExerciseCommand extends Command {

  private final ExerciseService exerciseService;
  private final UserStateService userStateService;

  private static final String SHOW_EXERCISE_RESULT_HISTORY_BUTTON_TEXT = "SHOW_RESULT_HISTORY";
  private static final String ADD_EXERCISE_RESULT_BUTTON_TEXT = "ADD_RESULT";
  private static final String EXERCISE_TEXT = "Посмотрите видео с упражнением на YouTube: [Смотреть видео](%s)";
  static final String SHOW_EXERCISE_RESULT_HISTORY = "E_HISTORY";
  static final String ADD_EXERCISE_RESULT_TEXT = "ADD_RESULT";

  public ExerciseCommand(
      KafkaTemplate<Long, BotApiMethodInterface> kafkaTemplate,
      KafkaExerciseService kafkaExerciseService, ExerciseService exerciseService,
      UserStateService userStateService) {
    super(kafkaTemplate, kafkaExerciseService);
    this.exerciseService = exerciseService;
    this.userStateService = userStateService;
  }

  @Override
  public boolean supports(UserInputCommandEvent event) {
    var chatId = event.chatId();
    var buttonCode = AnswerData.deserialize(event.update().getMessage()).getButtonCode();

    var userState = userStateService.getCurrentState(chatId).orElse(null);
    return userState != null
        && UserStateType.CHOOSING_EXERCISE.equals(userState.getUserStateType())
        && "exercise".equals(buttonCode);
  }

  @Override
  public void process(UserInputCommandEvent event) {
    var chatId = event.chatId();

    var exerciseName = AnswerData.deserialize(event.update().getMessage()).getButtonText();
    var exercise = exerciseService.getExerciseByName(exerciseName);

    List<AnswerDto> answerDtoList = new ArrayList<>();
    answerDtoList.add(new AnswerDto(SHOW_EXERCISE_RESULT_HISTORY_BUTTON_TEXT, SHOW_EXERCISE_RESULT_HISTORY));
    answerDtoList.add(new AnswerDto(ADD_EXERCISE_RESULT_BUTTON_TEXT, ADD_EXERCISE_RESULT_TEXT));

    var userState = userStateService.getCurrentState(chatId).orElse(new UserState());
    userState.setUserStateType(UserStateType.VIEWING_EXERCISE);
    userState.setCurrentExercise(exercise);
    userStateService.setCurrentState(chatId, userState);

    kafkaExerciseService.sendBotApiMethod(event.chatId(),
            EditMessageWrapper.newBuilder()
                .chatId(chatId)
                .messageId(event.update().getMessageId())
                .replyMarkup(KeyboardMarkupUtil.createRows(answerDtoList, 1))
                .text(String.format("%s%n" + EXERCISE_TEXT, exercise.getDescription(), exercise.getVideoUrl()))
                .parseMode("MarkdownV2")
                .disableWebPagePreview(false)
                .build())
        .subscribe();
  }
}
