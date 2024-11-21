package microservice.service.command;

import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.wrapper.BotApiMethodWrapper;
import kvansipto.exercise.wrapper.SendMessageWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microservice.service.ExerciseResultService;
import microservice.service.UserService;
import microservice.service.event.UserInputCommandEvent;
import microservice.service.user.state.UserStateService;
import microservice.service.user.state.UserStateType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddResultForExerciseResultCommand extends Command {

  private final UserService userService;
  private final ExerciseResultService exerciseResultService;
  private final UserStateService userStateService;

  private static final String SAVE_RESULT_SUCCESS_TEXT = "Результат успешно сохранен";

  @Override
  public boolean supports(UserInputCommandEvent event) {
    var chatId = event.chatId();

    var userState = userStateService.getCurrentState(chatId).orElse(null);
    return userState != null
        && UserStateType.WAITING_FOR_RESULT.equals(userState.getUserStateType());
  }

  @Override
  public void process(UserInputCommandEvent event) {
    var message = event.update().getMessage();
    var chatId = event.chatId();

    var botApiMethodWrapper = new BotApiMethodWrapper();
    var sendMessageWrapperBuilder = SendMessageWrapper.newBuilder().chatId(chatId);

    String[] parts = message.split(" ", 4);
    var weight = Double.parseDouble(parts[0]);
    var sets = Integer.parseInt(parts[1]);
    var reps = Integer.parseInt(parts[2]);
    var comment = (parts.length == 4) ? parts[3] : null;

    log.info("Data was parsed into: weight={}, sets={}, reps={}, comment={}", weight, sets, reps, comment);

    var userState = userStateService.getCurrentState(chatId).orElseThrow();
    var exerciseResult = ExerciseResultDto.builder()
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

    botApiMethodWrapper.addAction(sendMessageWrapperBuilder.build());
    kafkaExerciseService.sendBotApiMethod(event.chatId(), botApiMethodWrapper).subscribe();
  }
}
