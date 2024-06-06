package kvansipto.telegram.microservice.services.command;

import java.util.ArrayList;
import java.util.List;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.UserStateFactory;
import kvansipto.telegram.microservice.services.UserStateService;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodWrapper;
import kvansipto.telegram.microservice.services.wrapper.DeleteMessagesWrapper;
import kvansipto.telegram.microservice.services.wrapper.SendMessageWrapper;
import kvansipto.telegram.microservice.services.wrapper.SendMessageWrapper.SendMessageWrapperBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class AddResultForExerciseResultCommand extends Command {

  @Autowired
  RestToExercises restToExercises;
  @Autowired
  UserStateFactory userStateFactory;
  @Autowired
  UserStateService userStateService;

  public static final String SAVE_RESULT_SUCCESS_TEXT = "Результат успешно сохранен";
  public static final String SAVE_RESULT_FAIL_TEXT = "Неверный формат ввода. Пожалуйста, введите данные снова.";

  private final List<Integer> wrongAttempts = new ArrayList<>();

  @Override
  public boolean supports(Update update) {
    return update.hasMessage() && userStateService.getCurrentState(update.getMessage().getChatId().toString()) != null
        && userStateService.getCurrentState(update.getMessage().getChatId().toString()).getCurrentState()
        .equals(AddExerciseResultCommand.WAITING_FOR_RESULT_STATE_TEXT);
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    var message = update.getMessage().getText();
    var chatId = update.getMessage().getChatId().toString();

    BotApiMethodWrapper botApiMethodWrapper = new BotApiMethodWrapper();
    SendMessageWrapperBuilder sendMessageWrapperBuilder = SendMessageWrapper.newBuilder()
        .chatId(chatId);
    try {
      String[] parts = message.split(" ");
      double weight = Double.parseDouble(parts[0]);
      byte sets = Byte.parseByte(parts[1]);
      byte reps = Byte.parseByte(parts[2]);
      ExerciseResultDto exerciseResult = ExerciseResultDto.builder()
          .weight(weight)
          .numberOfSets(sets)
          .numberOfRepetitions(reps)
          .user(restToExercises.getUser(chatId))
          .exercise(userStateService.getCurrentState(chatId).getCurrentExercise())
          .date(userStateService.getCurrentState(chatId).getExerciseResultDate())
          .build();
      restToExercises.saveExerciseResult(exerciseResult);
      sendMessageWrapperBuilder.text(SAVE_RESULT_SUCCESS_TEXT);
      userStateService.removeUserState(chatId);

      if (!wrongAttempts.isEmpty()) {
        DeleteMessagesWrapper deleteMessagesWrapper = new DeleteMessagesWrapper(chatId, new ArrayList<>(wrongAttempts));
        botApiMethodWrapper.addAction(deleteMessagesWrapper);
        wrongAttempts.clear();
      }
    } catch (Exception e) {
      wrongAttempts.add(update.getMessage().getMessageId());
      wrongAttempts.add(update.getMessage().getMessageId() + 1);
      sendMessageWrapperBuilder.text(SAVE_RESULT_FAIL_TEXT);
    }
    botApiMethodWrapper.addAction(sendMessageWrapperBuilder.build());
    return botApiMethodWrapper;
  }
}
