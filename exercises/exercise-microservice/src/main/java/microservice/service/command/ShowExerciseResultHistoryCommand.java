package microservice.service.command;

import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.filter.ExerciseResultFilter;
import kvansipto.exercise.wrapper.BotApiMethodInterface;
import kvansipto.exercise.wrapper.BotApiMethodWrapper;
import kvansipto.exercise.wrapper.EditMessageWrapper;
import lombok.extern.slf4j.Slf4j;
import microservice.service.ExerciseResultService;
import microservice.service.KafkaExerciseService;
import microservice.service.TableImageService;
import microservice.service.dto.AnswerData;
import microservice.service.event.UserInputCommandEvent;
import microservice.service.user.state.UserStateService;
import microservice.service.user.state.UserStateType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ShowExerciseResultHistoryCommand extends Command {

  private static final String EMPTY_LIST_EXERCISE_RESULT_TEXT = "Результаты по упражнению отсутствуют";
  private static final String EXERCISE_RESULT_TEXT = "Результаты по упражнению %s";
  private static final String[] HEADERS = {"Дата", "Вес (кг)", "Подходы", "Повторения", "Комментарий"};

  private final TableImageService tableImageService;
  private final UserStateService userStateService;
  private final ExerciseResultService exerciseResultService;

  public ShowExerciseResultHistoryCommand(
      KafkaTemplate<Long, BotApiMethodInterface> kafkaTemplate,
      KafkaExerciseService kafkaExerciseService, TableImageService tableImageService,
      UserStateService userStateService, ExerciseResultService exerciseResultService) {
    super(kafkaTemplate, kafkaExerciseService);
    this.tableImageService = tableImageService;
    this.userStateService = userStateService;
    this.exerciseResultService = exerciseResultService;
  }

  @Override
  public boolean supports(UserInputCommandEvent event) {
    var chatId = event.chatId();
    var buttonCode = AnswerData.deserialize(event.update().getMessage()).getButtonCode();

    var userState = userStateService.getCurrentState(chatId).orElse(null);

    return userState != null
        && UserStateType.VIEWING_EXERCISE.equals(userState.getUserStateType())
        && ExerciseCommand.SHOW_EXERCISE_RESULT_HISTORY.equals(buttonCode);
  }

  @Override
  public void process(UserInputCommandEvent event) {
    var chatId = event.chatId();

    var exercise = userStateService.getCurrentState(event.chatId()).orElseThrow().getCurrentExercise();

    var exerciseResultFilter = ExerciseResultFilter.builder()
        .exerciseDto(exercise)
        .userChatId(chatId)
        .build();
    List<ExerciseResultDto> exerciseResults = exerciseResultService.findExerciseResults(exerciseResultFilter);

    var botApiMethodWrapper = new BotApiMethodWrapper();
    if (exerciseResults.isEmpty()) {
      botApiMethodWrapper.addAction(
          EditMessageWrapper.newBuilder()
              .chatId(chatId)
              .messageId(event.update().getMessageId())
              .text(EMPTY_LIST_EXERCISE_RESULT_TEXT)
              .build());
    } else {
      var dtf = DateTimeFormatter.ofPattern("dd/MM");
      String[][] data = new String[exerciseResults.size()][HEADERS.length];
      for (int i = 0; i < exerciseResults.size(); i++) {
        var exerciseResult = exerciseResults.get(i);
        data[i] = new String[]{
            exerciseResult.getDate().format(dtf),
            String.valueOf(exerciseResult.getWeight()),
            String.valueOf(exerciseResult.getNumberOfSets()),
            String.valueOf(exerciseResult.getNumberOfRepetitions()),
            exerciseResult.getComment()
        };
      }
      // TODO Асинхронно генерировать таблицу и отправлять, когда она будет готова
      byte[] imageBytes = tableImageService.drawTableImage(HEADERS, data);
      kafkaExerciseService.sendMedia(chatId, Base64.getEncoder().encodeToString(imageBytes)).subscribe();

      botApiMethodWrapper.addAction(EditMessageWrapper.newBuilder()
          .chatId(chatId)
          .text(String.format(EXERCISE_RESULT_TEXT, exercise.getName()))
          .messageId(event.update().getMessageId())
          .build());
    }
    kafkaExerciseService.sendBotApiMethod(chatId, botApiMethodWrapper).subscribe();
    userStateService.removeUserState(chatId);
  }
}
