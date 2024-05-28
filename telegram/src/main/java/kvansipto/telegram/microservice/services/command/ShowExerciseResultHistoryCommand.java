package kvansipto.telegram.microservice.services.command;

import java.time.format.DateTimeFormatter;
import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodWrapper;
import kvansipto.telegram.microservice.services.wrapper.SendMessageWrapper;
import kvansipto.telegram.microservice.services.wrapper.SendMessageWrapper.SendMessageWrapperBuilder;
import kvansipto.telegram.microservice.services.wrapper.SendPhotoWrapper;
import kvansipto.telegram.microservice.utils.TableImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class ShowExerciseResultHistoryCommand extends Command {

  public static final String EMPTY_LIST_EXERCISE_RESULT_TEXT = "Результаты по упражнению %s отсутствуют";
  public static final String EXERCISE_RESULT_HISTORY_LIST_TEXT = "История результатов упражнения %s";
  public static final String[] HEADERS = {"Дата", "Вес (кг)", "Подходы", "Повторения"};

  @Autowired
  RestToExercises restToExercises;

  @Override
  public boolean supports(Update update) {
    return update.hasCallbackQuery() && update.getCallbackQuery().getData()
        .startsWith(ExerciseCommand.SHOW_EXERCISE_RESULT_HISTORY);
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    String exerciseName = update.getCallbackQuery().getData().split("_")[4];
    ExerciseDto exercise = restToExercises.getExerciseByName(exerciseName);
//    Exercise exercise = exerciseRepository.findByName(exerciseName);
    String chatId = update.getCallbackQuery().getMessage().getChatId().toString();

    List<ExerciseResultDto> exerciseResults = restToExercises.getExerciseResults(exercise, chatId);
//        exerciseResultRepository.findByExerciseAndUserChatIdOrderByDateDesc(exercise, chatId);
    SendMessageWrapperBuilder sendMessageWrapperBuilder = SendMessageWrapper.newBuilder()
        .chatId(chatId);
    if (exerciseResults.isEmpty()) {
      sendMessageWrapperBuilder.text(EMPTY_LIST_EXERCISE_RESULT_TEXT.formatted(exerciseName));
      return sendMessageWrapperBuilder.build();
    } else {
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");

      String[][] data = new String[exerciseResults.size()][HEADERS.length];

      for (int i = 0; i < exerciseResults.size(); i++) {
        ExerciseResultDto exerciseResult = exerciseResults.get(i);
        data[i] = new String[]{exerciseResult.getDate().format(dtf), String.valueOf(exerciseResult.getWeight()),
            String.valueOf(exerciseResult.getNumberOfSets()), String.valueOf(exerciseResult.getNumberOfRepetitions())};
      }

      sendMessageWrapperBuilder.text(EXERCISE_RESULT_HISTORY_LIST_TEXT.formatted(exerciseName));
      BotApiMethodWrapper botApiMethodWrapper = new BotApiMethodWrapper();
      botApiMethodWrapper.addAction(sendMessageWrapperBuilder.build());

      var input = new InputFile().setMedia(TableImage.drawTableImage(HEADERS, data));
      SendPhotoWrapper sendPhoto = SendPhotoWrapper.newBuilder()
          .chatId(chatId)
          .photo(input)
          .build();
      botApiMethodWrapper.addAction(sendPhoto);
      return botApiMethodWrapper;
    }
  }
}
