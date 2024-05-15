package io.project.kvansiptobot.service.command;

import io.project.kvansiptobot.model.Exercise;
import io.project.kvansiptobot.repository.ExerciseRepository;
import io.project.kvansiptobot.service.wrapper.BotApiMethodInterface;
import io.project.kvansiptobot.service.wrapper.SendMessageWrapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
public class ExerciseCommand extends Command {

  @Autowired
  ExerciseRepository exerciseRepository;

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

    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
    List<InlineKeyboardButton> row = new ArrayList<>();
    InlineKeyboardButton button = new InlineKeyboardButton();
    button.setText("Отобразить историю результатов по упражнению");
    button.setCallbackData(SHOW_EXERCISE_RESULT_HISTORY + exercise.getName());
    row.add(button);
    rows.add(row);
    row = new ArrayList<>();
    button = new InlineKeyboardButton();
    button.setCallbackData(ADD_EXERCISE_RESULT_TEXT + exercise.getName());
    button.setText("Добавить результат выполнения упражнения");
    row.add(button);
    rows.add(row);
    inlineKeyboardMarkup.setKeyboard(rows);

    return SendMessageWrapper.newBuilder()
        .chatId(chatId)
        .replyMarkup(inlineKeyboardMarkup)
        .text(String.format("Посмотрите видео с упражнением на YouTube: [Смотреть видео](%s)",
            exercise.getVideoUrl()))
        .parseMode("MarkdownV2")
        .disableWebPagePreview(false)
        .build();
  }
}
