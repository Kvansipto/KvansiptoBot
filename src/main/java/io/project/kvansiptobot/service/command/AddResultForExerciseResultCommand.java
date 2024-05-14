package io.project.kvansiptobot.service.command;

import io.project.kvansiptobot.model.ExerciseResult;
import io.project.kvansiptobot.repository.ExerciseRepository;
import io.project.kvansiptobot.repository.ExerciseResultRepository;
import io.project.kvansiptobot.repository.UserRepository;
import io.project.kvansiptobot.service.UserStateFactory;
import io.project.kvansiptobot.service.UserStateService;
import io.project.kvansiptobot.service.wrapper.BotApiMethodInterface;
import io.project.kvansiptobot.service.wrapper.SendMessageWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class AddResultForExerciseResultCommand extends Command {

  @Autowired
  ExerciseRepository exerciseRepository;
  @Autowired
  ExerciseResultRepository exerciseResultRepository;
  @Autowired
  UserRepository userRepository;
  @Autowired
  UserStateFactory userStateFactory;
  @Autowired
  UserStateService userStateService;

  @Override
  public boolean supports(Update update) {
    return update.hasMessage() && userStateService.getCurrentState(update.getMessage().getChatId()) != null
        && userStateService.getCurrentState(update.getMessage().getChatId()).getCurrentState()
        .equals(AddExerciseResultCommand.WAITING_FOR_RESULT_STATE_TEXT);
  }

  @Override
  public BotApiMethodInterface process(Update update) {

    var message = update.getMessage().getText();
    var chatId = update.getMessage().getChatId();
    String[] parts = message.split(" ");
    double weight = Double.parseDouble(parts[0]);
    byte sets = Byte.parseByte(parts[1]);
    byte reps = Byte.parseByte(parts[2]);
    ExerciseResult exerciseResult = ExerciseResult.builder()
        .weight(weight)
        .numberOfSets(sets)
        .numberOfRepetitions(reps)
        .user(userRepository.findById(chatId).get())
        .exercise(userStateService.getCurrentState(chatId).getCurrentExercise())
        .date(userStateService.getCurrentState(chatId).getExerciseResultDate())
        .build();

    SendMessageWrapper sendMessageWrapper = new SendMessageWrapper();
    sendMessageWrapper.setChatId(chatId);
    try {
      exerciseResultRepository.save(exerciseResult);
      sendMessageWrapper.setText("Результат успешно сохранен");
      userStateService.removeUserState(chatId);
    } catch (Exception e) {
      sendMessageWrapper.setText("Неверный формат ввода\\. Пожалуйста, введите данные снова\\.");
    }
    return sendMessageWrapper;
  }
}
