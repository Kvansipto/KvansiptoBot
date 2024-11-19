package kvansipto.telegram.microservice.services.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.dto.UserDto;
import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.UserState;
import kvansipto.telegram.microservice.services.UserStateService;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodWrapper;
import kvansipto.telegram.microservice.services.wrapper.SendMessageWrapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddResultForExerciseResultCommandTest {

  @InjectMocks
  private AddResultForExerciseResultCommand addResultForExerciseResultCommand;

  @Mock
  private static RestToExercises restToExercises;
  @Mock
  UserStateService userStateService;
  @Mock
  private static UserState userState;

  private static Update update;
  private static Message message;
  private static ExerciseDto exercise;

  @BeforeAll
  static void setUp() {
    Chat chat = new Chat();
    chat.setId(123456L);

    message = new Message();
    message.setText("60.5 3 12");
    message.setChat(chat);
    update = new Update();
    update.setMessage(message);

    exercise = ExerciseDto.builder().build();

    //TODO почему получаю NPE на userStateService, если userStateService будет статическим?
//    when(userStateService.getCurrentState(anyString())).thenReturn(userState);
//    when(userState.getCurrentState()).thenReturn(AddExerciseResultCommand.WAITING_FOR_RESULT_STATE_TEXT);
//    when(userState.getCurrentExercise()).thenReturn(exercise);
  }

  @Test
  void supports_shouldReturnTrue_whenUpdateHasMessageAndUserStateIsWaitingForResult() {
    when(userStateService.getCurrentState(anyLong())).thenReturn(userState);
    when(userState.getCurrentState()).thenReturn(AddExerciseResultCommand.WAITING_FOR_RESULT_STATE_TEXT);
    when(userState.getCurrentExercise()).thenReturn(exercise);
    boolean result = addResultForExerciseResultCommand.supports(update);
    assertTrue(result);
  }

  @Test
  void process_shouldReturnBotApiMethodWrapper_whenInputIsValid() {
    when(userStateService.getCurrentState(anyLong())).thenReturn(userState);
    when(userState.getCurrentState()).thenReturn(AddExerciseResultCommand.WAITING_FOR_RESULT_STATE_TEXT);
    when(userState.getCurrentExercise()).thenReturn(exercise);
    UserDto user = UserDto.builder().build();
    // Arrange
    when(restToExercises.getUser(anyLong())).thenReturn(user);
    when(userState.getExerciseResultDate()).thenReturn(LocalDate.now());

    // Act
    BotApiMethodInterface result = addResultForExerciseResultCommand.process(update);

    // Assert
    assertThat(result).isInstanceOf(BotApiMethodWrapper.class);
    BotApiMethodWrapper botApiMethodWrapper = (BotApiMethodWrapper) result;
    List<BotApiMethodInterface> actions = botApiMethodWrapper.getActions();

    assertThat(actions).hasSize(1);
    assertThat(actions.get(0)).isInstanceOf(SendMessageWrapper.class);
    SendMessageWrapper sendMessageWrapper = (SendMessageWrapper) actions.get(0);

    assertThat(sendMessageWrapper.getNumberChatId()).isEqualTo(123456L);
    assertThat(sendMessageWrapper.getText()).isEqualTo(AddResultForExerciseResultCommand.SAVE_RESULT_SUCCESS_TEXT);

    // Verify user state changes
    verify(userStateService, times(1)).removeUserState(123456L);
    verify(restToExercises, times(1)).saveExerciseResult(any(ExerciseResultDto.class));
  }

  @Test
  void process_shouldReturnBotApiMethodWrapperWithErrorMessage_whenInputIsInvalid() {
    // Arrange
    message.setText("invalid input");
    message.setMessageId(new Random(5).nextInt());
    update.setMessage(message);
    when(userStateService.getCurrentState(anyLong())).thenReturn(userState);
    when(userState.getCurrentState()).thenReturn(AddExerciseResultCommand.WAITING_FOR_RESULT_STATE_TEXT);
    when(userState.getCurrentExercise()).thenReturn(exercise);

    // Act
    BotApiMethodInterface result = addResultForExerciseResultCommand.process(update);

    // Assert
    assertThat(result).isInstanceOf(BotApiMethodWrapper.class);
    BotApiMethodWrapper botApiMethodWrapper = (BotApiMethodWrapper) result;
    List<BotApiMethodInterface> actions = botApiMethodWrapper.getActions();

    assertThat(actions).hasSize(1);
    assertThat(actions.get(0)).isInstanceOf(SendMessageWrapper.class);
    SendMessageWrapper sendMessageWrapper = (SendMessageWrapper) actions.get(0);

    assertThat(sendMessageWrapper.getChatId()).isEqualTo("123456");
    assertThat(sendMessageWrapper.getText()).isEqualTo(AddResultForExerciseResultCommand.SAVE_RESULT_FAIL_TEXT);
  }
}
