package microservice.service.command;

import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.wrapper.BotApiMethodWrapper;
import kvansipto.exercise.wrapper.SendMessageWrapper;
import kvansipto.exercise.wrapper.SendMessageWrapper.SendMessageWrapperBuilder;
import microservice.service.ExerciseResultService;
import microservice.service.UserService;
import microservice.service.event.UserInputCommandEvent;
import microservice.service.user.state.UserState;
import microservice.service.user.state.UserStateService;
import microservice.service.user.state.UserStateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddResultForExerciseResultCommand extends Command {

  private static final Logger log = LoggerFactory.getLogger(AddResultForExerciseResultCommand.class);
  @Autowired
  private UserService userService;
  @Autowired
  private ExerciseResultService exerciseResultService;
  @Autowired
  private UserStateService userStateService;

  public static final String SAVE_RESULT_SUCCESS_TEXT = "Результат успешно сохранен";

  @Override
  public boolean supports(UserInputCommandEvent event) {
    Long chatId = event.chatId();

    UserState userState = userStateService.getCurrentState(chatId).orElse(null);
    return userState != null
        && UserStateType.WAITING_FOR_RESULT.equals(userState.getUserStateType());
  }

  @Override
  public void process(UserInputCommandEvent event) {
    var message = event.update().getMessage();
    var chatId = event.chatId();

    BotApiMethodWrapper botApiMethodWrapper = new BotApiMethodWrapper();
    SendMessageWrapperBuilder sendMessageWrapperBuilder = SendMessageWrapper.newBuilder().chatId(chatId);

    String[] parts = message.split(" ", 4);
    double weight = Double.parseDouble(parts[0]);
    int sets = Integer.parseInt(parts[1]);
    int reps = Integer.parseInt(parts[2]);
    String comment = (parts.length == 4) ? parts[3] : null;

    log.info("Data was parsed into: weight={}, sets={}, reps={}, comment={}", weight, sets, reps, comment);

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

    botApiMethodWrapper.addAction(sendMessageWrapperBuilder.build());
    kafkaExerciseService.sendBotApiMethod(event.chatId(), botApiMethodWrapper).subscribe();
  }
}
