package kvansipto.telegram.microservice.services.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.dto.UserDto;
import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.UserState;
import kvansipto.telegram.microservice.services.UserStateService;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodWrapper;
import kvansipto.telegram.microservice.services.wrapper.SendMessageWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddResultForExerciseResultCommandTest {

  @InjectMocks
  private AddResultForExerciseResultCommand addResultForExerciseResultCommand;

  @Mock
  private RestToExercises restToExercises;
  @Mock
  private UserStateService userStateService;
  @Mock
  private Update update;
  @Mock
  private Message message;
  @Mock
  private UserState userState;
  @Mock
  private ExerciseDto exercise;
  @Mock
  UserDto user;

  @BeforeEach
  void setUp() {
    when(update.hasMessage()).thenReturn(true);
    when(update.getMessage()).thenReturn(message);
    when(message.getChatId()).thenReturn(123456L);
    when(message.getText()).thenReturn("60.5 3 12");
    when(userStateService.getCurrentState(anyString())).thenReturn(userState);
    when(userState.getCurrentState()).thenReturn(AddExerciseResultCommand.WAITING_FOR_RESULT_STATE_TEXT);
    when(userState.getCurrentExercise()).thenReturn(exercise);
  }

  @Test
  void supports_shouldReturnTrue_whenUpdateHasMessageAndUserStateIsWaitingForResult() {
    boolean result = addResultForExerciseResultCommand.supports(update);
    assertTrue(result);
  }

  @Test
  void process_shouldReturnBotApiMethodWrapper_whenInputIsValid() {
    // Arrange
    when(restToExercises.getUser(anyString())).thenReturn(user);  // Mock user object
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

    assertThat(sendMessageWrapper.getChatId()).isEqualTo("123456");
    assertThat(sendMessageWrapper.getText()).isEqualTo(AddResultForExerciseResultCommand.SAVE_RESULT_SUCCESS_TEXT);

    // Verify user state changes
    verify(userStateService, times(1)).removeUserState("123456");
    verify(restToExercises, times(1)).saveExerciseResult(any(ExerciseResultDto.class));
  }

  @Test
  void process_shouldReturnBotApiMethodWrapperWithErrorMessage_whenInputIsInvalid() {
    // Arrange
    when(message.getText()).thenReturn("invalid input");

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
