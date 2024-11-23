package microservice.service.command.menu;

import java.util.Arrays;
import java.util.List;
import kvansipto.exercise.wrapper.BotApiMethodInterface;
import kvansipto.exercise.wrapper.SendMessageWrapper;
import microservice.entity.MuscleGroup;
import microservice.service.KafkaExerciseService;
import microservice.service.KeyboardMarkupUtil;
import microservice.service.dto.AnswerDto;
import microservice.service.event.UserInputCommandEvent;
import microservice.service.user.state.UserStateFactory;
import microservice.service.user.state.UserStateService;
import microservice.service.user.state.UserStateType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component("/exercise_info")
public class MuscleGroupCommand extends MainMenuCommand {

  private final UserStateService userStateService;
  private final UserStateFactory userStateFactory;

  public MuscleGroupCommand(
      KafkaTemplate<Long, BotApiMethodInterface> kafkaTemplate,
      KafkaExerciseService kafkaExerciseService, UserStateService userStateService,
      UserStateFactory userStateFactory) {
    super(kafkaTemplate, kafkaExerciseService);
    this.userStateService = userStateService;
    this.userStateFactory = userStateFactory;
  }

  @Override
  public void process(UserInputCommandEvent event) {
    var chatId = event.chatId();

    var userState = userStateService.getCurrentState(chatId)
        .orElseGet(() -> userStateFactory.createUserSession(chatId));

    userState.setUserStateType(UserStateType.CHOOSING_MUSCLE_GROUP);

    userStateService.setCurrentState(chatId, userState);

    List<AnswerDto> answerDtoList = Arrays.stream(MuscleGroup.values())
        .map(muscleGroup -> new AnswerDto(muscleGroup.getName(), "muscle_group"))
        .toList();

    kafkaExerciseService.sendBotApiMethod(event.chatId(),
            SendMessageWrapper.newBuilder()
                .chatId(chatId)
                .replyMarkup(KeyboardMarkupUtil.createRows(answerDtoList, 2))
                .text("Выберите группу мышц")
                .build())
        .subscribe();
  }

  @Override
  public String explanation() {
    return "to show muscle groups";
  }
}
