package microservice.service.command;

import java.util.List;
import kvansipto.exercise.wrapper.EditMessageWrapper;
import microservice.service.ExerciseService;
import microservice.service.KeyboardMarkupUtil;
import microservice.service.UserState;
import microservice.service.UserStateService;
import microservice.service.dto.AnswerData;
import microservice.service.dto.AnswerDto;
import microservice.service.event.UserInputCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MuscleCommand extends Command {

  @Autowired
  private ExerciseService exerciseService;

  @Autowired
  private UserStateService userStateService;

  public static final String MUSCLE_COMMAND_TEXT = "Выберите упражнение";

  @Override
  public boolean supports(UserInputCommandEvent event) {
    Long chatId = event.chatId();

    String buttonCode = AnswerData.deserialize(event.update().getMessage()).getButtonCode();

    UserState userState = userStateService.getCurrentState(chatId).orElse(null);

    return userState != null
        && "CHOOSING MUSCLE GROUP".equals(userState.getCurrentState())
        && "muscle_group".equals(buttonCode);
  }

  @Override
  public void process(UserInputCommandEvent event) {
    Long chatId = event.chatId();

    String muscleGroup = AnswerData.deserialize(event.update().getMessage()).getButtonText();
    System.out.println("Группа мышц: " + muscleGroup);

    List<AnswerDto> answerDtoList = exerciseService.getExercisesByMuscleGroup(muscleGroup).stream()
        .map(e -> new AnswerDto(e.getName(), "exercise"))
        .toList();

    System.out.println("Упражнения для группы мышц " + muscleGroup + ": " + answerDtoList);

    UserState userState = userStateService.getCurrentState(chatId).orElse(new UserState());
    userState.setCurrentState("CHOOSING EXERCISE");
    userStateService.setCurrentState(chatId, userState);

    kafkaTemplate.send("actions-from-exercises", event.chatId(),
        EditMessageWrapper.newBuilder()
            .chatId(chatId)
            .messageId(event.update().getMessageId())
            .replyMarkup(KeyboardMarkupUtil.createRows(answerDtoList, 1))
            .text(MUSCLE_COMMAND_TEXT)
            .build());
  }
}
