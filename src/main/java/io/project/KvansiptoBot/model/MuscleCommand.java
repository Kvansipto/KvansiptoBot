package io.project.KvansiptoBot.model;

import io.project.KvansiptoBot.service.MuscleCommandEvent;
import io.project.KvansiptoBot.service.TelegramBot;
import java.util.ArrayList;
import java.util.List;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
public class MuscleCommand implements Command {

  @Autowired
  ExerciseRepository exerciseRepository;
  @Autowired
  TelegramBot telegramBot;
  @Autowired
  ApplicationEventPublisher eventPublisher;
  @Setter
  private MuscleGroup muscleGroup;

  @Override
  public void execute(long chatId) {
    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
    List<InlineKeyboardButton> row = new ArrayList<>();

    var exercises = exerciseRepository.findByMuscleGroup(muscleGroup);
    for (Exercise exercise : exercises) {
      InlineKeyboardButton exerciseButton = new InlineKeyboardButton();
      exerciseButton.setText(exercise.getName());
      exerciseButton.setCallbackData(exercise.getName());
      row.add(exerciseButton);
    }
    rows.add(row);
    inlineKeyboardMarkup.setKeyboard(rows);
    System.out.println("Publishing MuscleCommandEvent");
    eventPublisher.publishEvent(
        new MuscleCommandEvent(this, chatId, "Выберите упражнение", inlineKeyboardMarkup));
  }
}
