package microservice.service.command.menu;

import java.util.Arrays;
import java.util.List;
import kvansipto.exercise.wrapper.SendMessageWrapper;
import microservice.entity.MuscleGroup;
import microservice.service.KeyboardMarkupUtil;
import microservice.service.UserState;
import microservice.service.UserStateFactory;
import microservice.service.UserStateService;
import microservice.service.dto.AnswerDto;
import microservice.service.event.UserInputCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("/exercise_info")
public class MuscleGroupCommand extends MainMenuCommand {

  @Autowired
  private UserStateService userStateService;

  @Autowired
  private UserStateFactory userStateFactory;

  @Override
  public void process(UserInputCommandEvent event) {
    Long chatId = event.chatId();

    UserState userState = userStateService.getCurrentState(chatId)
        .orElseGet(() -> userStateFactory.createUserSession(chatId));

    userState.setCurrentState("CHOOSING MUSCLE GROUP");

    userStateService.setCurrentState(chatId, userState);

    List<AnswerDto> answerDtoList = Arrays.stream(MuscleGroup.values())
        .map(muscleGroup -> new AnswerDto(muscleGroup.getName(), "muscle_group"))
        .toList();

    kafkaTemplate.send("actions-from-exercises", event.chatId(),
        SendMessageWrapper.newBuilder()
            .chatId(chatId)
            .replyMarkup(KeyboardMarkupUtil.createRows(answerDtoList, 2))
            .text("Выберите группу мышц")
            .build());
  }

  @Override
  public String explanation() {
    return "to show muscle groups";
  }
}
