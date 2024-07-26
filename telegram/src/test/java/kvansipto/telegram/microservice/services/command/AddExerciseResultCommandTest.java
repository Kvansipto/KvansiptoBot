package kvansipto.telegram.microservice.services.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.telegram.microservice.services.UserState;
import kvansipto.telegram.microservice.services.UserStateFactory;
import kvansipto.telegram.microservice.services.UserStateService;
import kvansipto.telegram.microservice.services.dto.AnswerData;
import kvansipto.telegram.microservice.services.dto.AnswerDto;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.EditMessageWrapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddExerciseResultCommandTest {

  @InjectMocks
  AddExerciseResultCommand addExerciseResultCommand;

  @Mock
  UserStateService userStateService;
  @Mock
  UserStateFactory userStateFactory;
  @Mock
  UserState userState;

  private static Update update;
  private static MockedStatic<AnswerData> mockedStaticAnswerData;
  private static ExerciseDto exercise;

  @BeforeAll
  static void setUp() {
    Chat chat = new Chat();
    chat.setId(123456L);
    Message message = new Message();
    message.setChat(chat);
    CallbackQuery callbackQuery = new CallbackQuery();
    callbackQuery.setMessage(message);
    callbackQuery.setData("mockData");
    update = new Update();
    update.setCallbackQuery(callbackQuery);
    update.setMessage(message);
    AnswerDto answerDto = new AnswerDto();
    answerDto.setButtonCode(AddDateForExerciseResultCommand.ADD_DATE_EXERCISE_RESULT_TEXT);
    answerDto.setHiddenText("01/01");
    exercise = ExerciseDto.builder().build();

    mockedStaticAnswerData = mockStatic(AnswerData.class);
    mockedStaticAnswerData.when(() -> AnswerData.deserialize("mockData"))
        .thenReturn(answerDto);
  }

  @AfterAll
  static void tearDown() {
    mockedStaticAnswerData.close();
  }

  @Test
  void supports_shouldReturnTrue_whenUpdateHasCallBackQueryWithAddDateExerciseResultText() {
    boolean result = addExerciseResultCommand.supports(update);
    assertTrue(result);
  }

  @Test
  void process_shouldReturnEditMessageWrapper() {
    // Arrange
    when(userStateService.getCurrentState(anyString())).thenReturn(userState);
    when(userState.getCurrentExercise()).thenReturn(exercise);
    when(userStateFactory.createUserSession(anyString())).thenReturn(userState);

    // Act
    BotApiMethodInterface result = addExerciseResultCommand.process(update);

    // Assert
    assertThat(result).isNotNull().isInstanceOf(EditMessageWrapper.class);
    EditMessageWrapper editMessageWrapper = (EditMessageWrapper) result;
    assertThat(editMessageWrapper.getChatId()).isEqualTo("123456");
    assertThat(editMessageWrapper.getText()).isEqualTo(AddExerciseResultCommand.ADD_EXERCISE_RESULT_TEXT);

    // Verify
    verify(userStateService, times(1)).getCurrentState(anyString());
    verify(userStateFactory, times(1)).createUserSession(anyString());
    verify(userStateService, times(1)).setCurrentState("123456", userState);
  }
}
