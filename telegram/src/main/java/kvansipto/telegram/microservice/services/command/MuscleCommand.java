package kvansipto.telegram.microservice.services.command;

import java.util.Arrays;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.MuscleGroupDto;
import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.SendMessageWrapper;
import kvansipto.telegram.microservice.utils.KeyboardMarkupUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class MuscleCommand extends Command {

  public static final String MUSCLE_COMMAND_TEXT = "Выберите упражнение";
  @Autowired
  RestToExercises restToExercises;

  @Override
  public boolean supports(Update update) {
    return update.hasCallbackQuery() && restToExercises.getMuscleGroups().stream()
        .map(MuscleGroupDto::getName)
        .anyMatch(m -> m.equals(update.getCallbackQuery().getData()));
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    String chatId = update.getCallbackQuery().getMessage().getChatId().toString();

    MuscleGroupDto muscleGroup = restToExercises.getMuscleGroups().stream()
        .filter(m -> m.getName().equals(update.getCallbackQuery().getData()))
        .findFirst().get();
    var dataToInlineKeyboardMarkup = restToExercises.getExercisesByMuscleGroup(muscleGroup).stream()
        .map(ExerciseDto::getName)
        .toList();
    //TODO Попробуй через EditMessage, чтобы уменьшить длину чата и сделать более похоже на приложение.
    return SendMessageWrapper.newBuilder()
        .chatId(chatId)
        .replyMarkup(KeyboardMarkupUtil.generateInlineKeyboardMarkup(dataToInlineKeyboardMarkup))
        .text(MUSCLE_COMMAND_TEXT)
        .build();
  }
}
