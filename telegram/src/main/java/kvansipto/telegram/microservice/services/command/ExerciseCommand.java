package kvansipto.telegram.microservice.services.command;

import java.util.ArrayList;
import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.dto.AnswerData;
import kvansipto.telegram.microservice.services.dto.AnswerDto;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.EditMessageWrapper;
import kvansipto.telegram.microservice.utils.KeyboardMarkupUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class ExerciseCommand extends Command {

  @Autowired
  RestToExercises restToExercises;

  public static final String SHOW_EXERCISE_RESULT_HISTORY_BUTTON_TEXT = "SHOW_RESULT_HISTORY";
  public static final String ADD_EXERCISE_RESULT_BUTTON_TEXT = "ADD_RESULT";
  public static final String EXERCISE_TEXT = "Посмотрите видео с упражнением на YouTube: [Смотреть видео](%s)";
  public static final String SHOW_EXERCISE_RESULT_HISTORY = "E_HISTORY";
  public static final String ADD_EXERCISE_RESULT_TEXT = "ADD_RESULT";

  @Override
  public boolean supports(Update update) {

    return update.hasCallbackQuery() &&
        AnswerData.deserialize(update.getCallbackQuery().getData()).getButtonCode().equals("exercise");
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
    ExerciseDto exercise =
        restToExercises.getExerciseByName(AnswerData.deserialize(update.getCallbackQuery().getData()).getButtonText());
    List<AnswerDto> answerDtoList = new ArrayList<>();
    answerDtoList.add(
        new AnswerDto(SHOW_EXERCISE_RESULT_HISTORY_BUTTON_TEXT, SHOW_EXERCISE_RESULT_HISTORY, exercise.getName()));
    answerDtoList.add(
        new AnswerDto(ADD_EXERCISE_RESULT_BUTTON_TEXT, ADD_EXERCISE_RESULT_TEXT, exercise.getName()));

    return EditMessageWrapper.newBuilder()
        .chatId(chatId)
        .messageId(update.getCallbackQuery().getMessage().getMessageId())
        .replyMarkup(KeyboardMarkupUtil.createRows(answerDtoList, 1))
        .text(String.format("%s%n" + EXERCISE_TEXT, exercise.getDescription(), exercise.getVideoUrl()))
        .parseMode("MarkdownV2")
        .disableWebPagePreview(false)
        .build();
  }
}
