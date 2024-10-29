package microservice.service.command;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.filter.ExerciseResultFilter;
import kvansipto.exercise.wrapper.BotApiMethodWrapper;
import kvansipto.exercise.wrapper.SendMessageWrapper;
import kvansipto.exercise.wrapper.SendPhotoWrapper;
import microservice.service.ExerciseResultService;
import microservice.service.TableImageService;
import microservice.service.UserState;
import microservice.service.UserStateService;
import microservice.service.dto.AnswerData;
import microservice.service.event.UserInputCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;

@Component
public class ShowExerciseResultHistoryCommand extends Command {

  private static final String EMPTY_LIST_EXERCISE_RESULT_TEXT = "Результаты по упражнению отсутствуют";
  private static final String[] HEADERS = {"Дата", "Вес (кг)", "Подходы", "Повторения", "Комментарий"};

  @Autowired
  private TableImageService tableImageService;

  @Autowired
  private UserStateService userStateService;

  @Autowired
  private ExerciseResultService exerciseResultService;

  @Override
  public boolean supports(UserInputCommandEvent event) {
    Long chatId = event.chatId();
    String buttonCode = AnswerData.deserialize(event.update().getMessage()).getButtonCode();

    UserState userState = userStateService.getCurrentState(chatId).orElse(null);

    return userState != null
        && "VIEWING EXERCISE".equals(userState.getCurrentState())
        && ExerciseCommand.SHOW_EXERCISE_RESULT_HISTORY.equals(buttonCode);
  }

  @Override
  public void process(UserInputCommandEvent event) {
    Long chatId = event.chatId();

    ExerciseDto exercise = userStateService.getCurrentState(event.chatId()).get().getCurrentExercise();
    UserState userState = userStateService.getCurrentState(chatId).orElse(new UserState());
    userState.setCurrentExercise(exercise);
    userState.setCurrentState("LOADING_HISTORY");
    userStateService.setCurrentState(chatId, userState);

    ExerciseResultFilter exerciseResultFilter = ExerciseResultFilter.builder()
        .exerciseDto(exercise)
        .userChatId(chatId)
        .build();
    List<ExerciseResultDto> exerciseResults = exerciseResultService.findExerciseResults(exerciseResultFilter);

    BotApiMethodWrapper botApiMethodWrapper = new BotApiMethodWrapper();

    if (exerciseResults.isEmpty()) {
      botApiMethodWrapper.addAction(
          SendMessageWrapper.newBuilder()
              .chatId(chatId)
              .text(EMPTY_LIST_EXERCISE_RESULT_TEXT)
              .build());
    } else {
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");
      String[][] data = new String[exerciseResults.size()][HEADERS.length];
      for (int i = 0; i < exerciseResults.size(); i++) {
        ExerciseResultDto exerciseResult = exerciseResults.get(i);
        data[i] = new String[]{
            exerciseResult.getDate().format(dtf),
            String.valueOf(exerciseResult.getWeight()),
            String.valueOf(exerciseResult.getNumberOfSets()),
            String.valueOf(exerciseResult.getNumberOfRepetitions()),
            exerciseResult.getComment()
        };
      }
      byte[] imageBytes = tableImageService.drawTableImage(HEADERS, data);
      InputStream is = new ByteArrayInputStream(imageBytes);
      botApiMethodWrapper.addAction(
          SendPhotoWrapper.newBuilder()
              .chatId(chatId)
              .photo(new InputFile(is, "table.png"))
              .build()
      );
    }
    kafkaTemplate.send("actions-from-exercises", event.chatId(), botApiMethodWrapper);
  }
}
