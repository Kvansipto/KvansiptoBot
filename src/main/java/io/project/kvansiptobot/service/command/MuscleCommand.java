package io.project.kvansiptobot.service.command;

import io.project.kvansiptobot.model.Exercise;
import io.project.kvansiptobot.model.MuscleGroup;
import io.project.kvansiptobot.repository.ExerciseRepository;
import io.project.kvansiptobot.service.wrapper.BotApiMethodInterface;
import io.project.kvansiptobot.service.wrapper.SendMessageWrapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
public class MuscleCommand extends Command {

  @Autowired
  ExerciseRepository exerciseRepository;

  @Override
  public boolean supports(Update update) {
    return update.hasCallbackQuery() && Arrays.stream(MuscleGroup.values())
        .map(MuscleGroup::getName)
        .anyMatch(m -> m.equals(update.getCallbackQuery().getData()));
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    long chatId = update.getCallbackQuery().getMessage().getChatId();
    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
    List<InlineKeyboardButton> row = new ArrayList<>();

    MuscleGroup muscleGroup = Arrays.stream(MuscleGroup.values())
        .filter(m -> m.getName().equals(update.getCallbackQuery().getData()))
        .findFirst().get();

    var exercises = exerciseRepository.findByMuscleGroup(muscleGroup);
    for (Exercise exercise : exercises) {
      InlineKeyboardButton exerciseButton = new InlineKeyboardButton();
      exerciseButton.setText(exercise.getName());
      exerciseButton.setCallbackData(exercise.getName());
      row.add(exerciseButton);
    }
    rows.add(row);
    inlineKeyboardMarkup.setKeyboard(rows);
    return SendMessageWrapper.newBuilder()
        .chatId(chatId)
        .replyMarkup(inlineKeyboardMarkup)
        .text("Выберите упражнение")
        .build();
  }
}
