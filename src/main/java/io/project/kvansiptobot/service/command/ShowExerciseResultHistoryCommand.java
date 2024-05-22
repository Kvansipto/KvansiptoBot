package io.project.kvansiptobot.service.command;

import io.project.kvansiptobot.model.Exercise;
import io.project.kvansiptobot.model.ExerciseResult;
import io.project.kvansiptobot.repository.ExerciseRepository;
import io.project.kvansiptobot.repository.ExerciseResultRepository;
import io.project.kvansiptobot.service.wrapper.BotApiMethodInterface;
import io.project.kvansiptobot.service.wrapper.BotApiMethodWrapper;
import io.project.kvansiptobot.service.wrapper.SendMessageWrapper;
import io.project.kvansiptobot.service.wrapper.SendMessageWrapper.SendMessageWrapperBuilder;
import io.project.kvansiptobot.service.wrapper.SendPhotoWrapper;
import io.project.kvansiptobot.utils.TableImage;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
  ExerciseRepository exerciseRepository;

  @Autowired
  ExerciseResultRepository exerciseResultRepository;

  @Override
  public boolean supports(Update update) {
    return update.hasCallbackQuery() && update.getCallbackQuery().getData()
        .startsWith(ExerciseCommand.SHOW_EXERCISE_RESULT_HISTORY);
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    String exerciseName = update.getCallbackQuery().getData().split("_")[4];
    Exercise exercise = exerciseRepository.findByName(exerciseName);
    Long chatId = update.getCallbackQuery().getMessage().getChatId();

    List<ExerciseResult> exerciseResults = exerciseResultRepository.findByExerciseAndUserChatIdOrderByDateDesc(exercise,
        chatId);
    SendMessageWrapperBuilder sendMessageWrapperBuilder = SendMessageWrapper.newBuilder()
        .chatId(chatId);
    if (exerciseResults.isEmpty()) {
      sendMessageWrapperBuilder.text(EMPTY_LIST_EXERCISE_RESULT_TEXT.formatted(exerciseName));
      return sendMessageWrapperBuilder.build();
    } else {
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");

      String[][] data = new String[exerciseResults.size()][HEADERS.length];

      for (int i = 0; i < exerciseResults.size(); i++) {
        ExerciseResult exerciseResult = exerciseResults.get(i);
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
