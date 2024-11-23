package microservice.service.command.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import kvansipto.exercise.dto.UpdateDto;
import kvansipto.exercise.dto.UserDto;
import kvansipto.exercise.wrapper.BotApiMethodInterface;
import kvansipto.exercise.wrapper.SendMessageWrapper;
import microservice.entity.MuscleGroup;
import microservice.service.KafkaExerciseService;
import microservice.service.event.UserInputCommandEvent;
import microservice.service.user.state.UserState;
import microservice.service.user.state.UserStateFactory;
import microservice.service.user.state.UserStateService;
import microservice.service.user.state.UserStateType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class MuscleGroupCommandTest {

  MuscleGroupCommand muscleGroupCommand;
  UserInputCommandEvent userInputCommandEvent;
  UpdateDto updateDto;
  Long chatId;
  UserDto user;

  @Mock
  private KafkaExerciseService kafkaExerciseService;
  @Mock
  private UserStateService userStateService;
  @Mock
  private UserStateFactory userStateFactory;
  @Mock
  private KafkaTemplate<Long, BotApiMethodInterface> kafkaTemplate;

  @BeforeEach
  void setUp() {
    chatId = 100L;
    user = new UserDto();
    user.setId(1L);

    muscleGroupCommand = new MuscleGroupCommand(kafkaTemplate, kafkaExerciseService, userStateService, userStateFactory);
    lenient().when(kafkaExerciseService.sendBotApiMethod(eq(chatId), any(SendMessageWrapper.class)))
        .thenReturn(Mono.empty());
    updateDto = new UpdateDto("/exercise_info", 1, user);
    userInputCommandEvent = new UserInputCommandEvent(this, chatId, updateDto);
  }

  @Test
  void supports_shouldReturnTrue_whenUpdateHasMessageWithExerciseInfoCommandText() {
    boolean result = muscleGroupCommand.supports(userInputCommandEvent);
    assertTrue(result, "supports() должен возвращать true для команды /exercise_info");
  }

  @Test
  void process_shouldCreateUserStateAndSendMuscleGroupSelectionMessage() {
    UserState newUserState = new UserState();
    when(userStateService.getCurrentState(chatId)).thenReturn(Optional.empty());
    when(userStateFactory.createUserSession(chatId)).thenReturn(newUserState);

    muscleGroupCommand.process(userInputCommandEvent);

    verify(userStateService, times(1)).setCurrentState(chatId, newUserState);
    assertEquals(UserStateType.CHOOSING_MUSCLE_GROUP, newUserState.getUserStateType());

    List<String> expectedButtons = getExpectedMuscleGroupButtons();
    verify(kafkaExerciseService, times(1)).sendBotApiMethod(eq(chatId),
        argThat(argument -> argument instanceof SendMessageWrapper &&
            isValidSendMessage((SendMessageWrapper) argument, expectedButtons))
    );
  }

  @Test
  void process_shouldUpdateExistingUserStateAndSendMuscleGroupSelectionMessage() {
    UserState existingUserState = new UserState();
    when(userStateService.getCurrentState(chatId)).thenReturn(Optional.of(existingUserState));

    muscleGroupCommand.process(userInputCommandEvent);

    verify(userStateService, times(1)).setCurrentState(chatId, existingUserState);
    assertEquals(UserStateType.CHOOSING_MUSCLE_GROUP, existingUserState.getUserStateType());

    List<String> expectedButtons = getExpectedMuscleGroupButtons();
    verify(kafkaExerciseService, times(1)).sendBotApiMethod(eq(chatId),
        argThat(argument -> argument instanceof SendMessageWrapper &&
            isValidSendMessage((SendMessageWrapper) argument, expectedButtons))
    );
  }

  @Test
  void explanation_shouldReturnRightMessage() {
    String result = muscleGroupCommand.explanation();
    assertEquals("to show muscle groups", result, "explanation() должен возвращать описание команды");
  }

  private boolean isValidSendMessage(SendMessageWrapper message, List<String> expectedButtons) {
    if (!message.getText().equals("Выберите группу мышц") || !message.getChatId().equals(chatId.toString())) {
      return false;
    }

    if (!(message.getReplyMarkup() instanceof InlineKeyboardMarkup markup)) {
      return false;
    }

    List<String> actualButtons = markup.getKeyboard().stream()
        .flatMap(List::stream)
        .map(InlineKeyboardButton::getText)
        .toList();

    return actualButtons.containsAll(expectedButtons) && actualButtons.size() == expectedButtons.size();
  }

  private List<String> getExpectedMuscleGroupButtons() {
    return Arrays.stream(MuscleGroup.values())
        .map(MuscleGroup::getName)
        .toList();
  }
}
