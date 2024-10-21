package kvansipto.telegram.microservice.services.command;

import java.time.format.DateTimeFormatter;
import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.dto.AnswerData;
import kvansipto.telegram.microservice.services.dto.ExerciseResultEvent;
import kvansipto.telegram.microservice.services.dto.TelegramActionEvent;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodWrapper;
import kvansipto.telegram.microservice.services.wrapper.EditMessageWrapper;
import kvansipto.telegram.microservice.services.wrapper.SendMessageWrapper;
import kvansipto.telegram.microservice.services.wrapper.SendPhotoWrapper;
import kvansipto.telegram.microservice.utils.TableImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class ShowExerciseResultHistoryCommand extends Command {

  private static final String EMPTY_LIST_EXERCISE_RESULT_TEXT = "Результаты по упражнению отсутствуют";
  private static final String[] HEADERS = {"Дата", "Вес (кг)", "Подходы", "Повторения", "Комментарий"};

  @Autowired
  RestToExercises restToExercises;

  @Autowired
  ApplicationEventPublisher eventPublisher;

  @Override
  public boolean supports(Update update) {
    return update.hasCallbackQuery() && AnswerData.deserialize(update.getCallbackQuery().getData()).getButtonCode()
        .equals(ExerciseCommand.SHOW_EXERCISE_RESULT_HISTORY);
  }

  @EventListener
  public void handleExerciseResultEvent(ExerciseResultEvent exerciseResultEvent) {
    Long chatId = exerciseResultEvent.chatId();
    List<ExerciseResultDto> exerciseResults = exerciseResultEvent.exerciseResults();

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
        data[i] = new String[]{exerciseResult.getDate().format(dtf), String.valueOf(exerciseResult.getWeight()),
            String.valueOf(exerciseResult.getNumberOfSets()),
            String.valueOf(exerciseResult.getNumberOfRepetitions()), exerciseResult.getComment()};
      }

      botApiMethodWrapper.addAction(
          SendPhotoWrapper.newBuilder()
              .chatId(chatId)
              .photo(new InputFile().setMedia(TableImage.drawTableImage(HEADERS, data)))
              .build()
      );
      TelegramActionEvent telegramActionEvent = new TelegramActionEvent(botApiMethodWrapper);
      eventPublisher.publishEvent(telegramActionEvent);
    }
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    String exerciseName = AnswerData.deserialize(update.getCallbackQuery().getData()).getHiddenText();
    ExerciseDto exercise = restToExercises.getExerciseByName(exerciseName);
    Long chatId = update.getCallbackQuery().getMessage().getChatId();

    restToExercises.requestExerciseResults(exercise, chatId);

    return EditMessageWrapper.newBuilder()
        .chatId(chatId)
        .messageId(update.getCallbackQuery().getMessage().getMessageId())
        .text(String.format("Загрузка данных по упражнению %s, пожалуйста, подождите...", exerciseName))
        .build();
  }
}
