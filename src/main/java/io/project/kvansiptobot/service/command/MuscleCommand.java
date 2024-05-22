package io.project.kvansiptobot.service.command;

import io.project.kvansiptobot.model.Exercise;
import io.project.kvansiptobot.model.MuscleGroup;
import io.project.kvansiptobot.repository.ExerciseRepository;
import io.project.kvansiptobot.service.wrapper.BotApiMethodInterface;
import io.project.kvansiptobot.service.wrapper.SendMessageWrapper;
import io.project.kvansiptobot.utils.KeyboardMarkupUtil;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

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

    MuscleGroup muscleGroup = Arrays.stream(MuscleGroup.values())
        .filter(m -> m.getName().equals(update.getCallbackQuery().getData()))
        .findFirst().get();
    var dataToInlineKeyboardMarkup = exerciseRepository.findByMuscleGroup(muscleGroup).stream()
        .map(Exercise::getName)
        .toList();
    //TODO Попробуй через EditMessage, чтобы уменьшить длину чата и сделать более похоже на приложение.
    return SendMessageWrapper.newBuilder()
        .chatId(chatId)
        .replyMarkup(KeyboardMarkupUtil.generateInlineKeyboardMarkup(dataToInlineKeyboardMarkup))
        .text("Выберите упражнение")
        .build();
  }
}
