package io.project.kvansiptobot.service.command;

import io.project.kvansiptobot.model.Exercise;
import io.project.kvansiptobot.repository.ExerciseRepository;
import io.project.kvansiptobot.service.wrapper.BotApiMethodInterface;
import io.project.kvansiptobot.service.wrapper.SendMessageWrapper;
import io.project.kvansiptobot.utils.KeyboardMarkupUtil;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class ExerciseCommand extends Command {

  @Autowired
  ExerciseRepository exerciseRepository;

  public static final String SHOW_EXERCISE_RESULT_HISTORY_BUTTON_TEXT = "Отобразить историю результатов по упражнению";
  public static final String ADD_EXERCISE_RESULT_BUTTON_TEXT = "Добавить результат выполнения упражнения";
  public static final String EXERCISE_TEXT = "Посмотрите видео с упражнением на YouTube: [Смотреть видео](%s)";
  public static final String SHOW_EXERCISE_RESULT_HISTORY = "SHOW_EXERCISE_RESULT_HISTORY_";
  public static final String ADD_EXERCISE_RESULT_TEXT = "ADD_EXERCISE_RESULT_";

  @Override
  public boolean supports(Update update) {
    return update.hasCallbackQuery() && exerciseRepository.existsByName(update.getCallbackQuery().getData());
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    long chatId = update.getCallbackQuery().getMessage().getChatId();
    Exercise exercise = exerciseRepository.findByName(update.getCallbackQuery().getData());

    Map<String, String> dataToInlineKeyboardMarkup = new HashMap<>();
    dataToInlineKeyboardMarkup.put(SHOW_EXERCISE_RESULT_HISTORY_BUTTON_TEXT,
        SHOW_EXERCISE_RESULT_HISTORY + exercise.getName());
    dataToInlineKeyboardMarkup.put(ADD_EXERCISE_RESULT_BUTTON_TEXT,
        ADD_EXERCISE_RESULT_TEXT + exercise.getName());
    return SendMessageWrapper.newBuilder()
        .chatId(chatId)
        .replyMarkup(KeyboardMarkupUtil.generateInlineKeyboardMarkup(dataToInlineKeyboardMarkup))
        .text(String.format("%s%n" + EXERCISE_TEXT, exercise.getDescription(), exercise.getVideoUrl()))
        .parseMode("MarkdownV2")
        .disableWebPagePreview(false)
        .build();
  }
}
