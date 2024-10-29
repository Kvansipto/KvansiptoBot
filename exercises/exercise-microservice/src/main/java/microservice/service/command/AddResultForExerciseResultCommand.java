package microservice.service.command;

import java.util.ArrayList;
import java.util.List;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.wrapper.BotApiMethodWrapper;
import kvansipto.exercise.wrapper.DeleteMessagesWrapper;
import kvansipto.exercise.wrapper.SendMessageWrapper;
import kvansipto.exercise.wrapper.SendMessageWrapper.SendMessageWrapperBuilder;
import microservice.service.ExerciseResultService;
import microservice.service.UserService;
import microservice.service.UserState;
import microservice.service.UserStateService;
import microservice.service.event.UserInputCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddResultForExerciseResultCommand extends Command {

  @Autowired
  private UserService userService;
  @Autowired
  private ExerciseResultService exerciseResultService;
  @Autowired
  private UserStateService userStateService;

  public static final String SAVE_RESULT_SUCCESS_TEXT = "Результат успешно сохранен";
  public static final String SAVE_RESULT_FAIL_TEXT = "Неверный формат ввода. Пожалуйста, введите данные снова.";

  private final List<Integer> wrongAttempts = new ArrayList<>();

  @Override
  public boolean supports(UserInputCommandEvent event) {
    Long chatId = event.chatId();

    UserState userState = userStateService.getCurrentState(chatId).orElse(null);
    return userState != null
        && AddExerciseResultCommand.WAITING_FOR_RESULT_STATE_TEXT.equals(userState.getCurrentState());
  }

  @Override
  public void process(UserInputCommandEvent event) {
    var message = event.update().getMessage();
    var chatId = event.chatId();

    BotApiMethodWrapper botApiMethodWrapper = new BotApiMethodWrapper();
    SendMessageWrapperBuilder sendMessageWrapperBuilder = SendMessageWrapper.newBuilder().chatId(chatId);

    try {
      String[] parts = message.split(" ", 4);
      double weight = Double.parseDouble(parts[0]);
      byte sets = Byte.parseByte(parts[1]);
      byte reps = Byte.parseByte(parts[2]);
      String comment = (parts.length == 4) ? parts[3] : null;

      UserState userState = userStateService.getCurrentState(chatId).orElseThrow();
      ExerciseResultDto exerciseResult = ExerciseResultDto.builder()
          .weight(weight)
          .numberOfSets(sets)
          .numberOfRepetitions(reps)
          .user(userService.getOne(chatId))
          .exercise(userState.getCurrentExercise())
          .date(userState.getExerciseResultDate())
          .comment(comment)
          .build();

      exerciseResultService.create(exerciseResult);
      sendMessageWrapperBuilder.text(SAVE_RESULT_SUCCESS_TEXT);

      userStateService.removeUserState(chatId);

      if (!wrongAttempts.isEmpty()) {
        DeleteMessagesWrapper deleteMessagesWrapper = new DeleteMessagesWrapper(chatId, new ArrayList<>(wrongAttempts));
        botApiMethodWrapper.addAction(deleteMessagesWrapper);
        wrongAttempts.clear();
      }
    } catch (Exception e) {
      // Добавление сообщения об ошибке в список неудачных попыток
      wrongAttempts.add(event.update().getMessageId());
      sendMessageWrapperBuilder.text(SAVE_RESULT_FAIL_TEXT);
    }
    botApiMethodWrapper.addAction(sendMessageWrapperBuilder.build());
    kafkaTemplate.send("actions-from-exercises", event.chatId(), botApiMethodWrapper);
  }
}
