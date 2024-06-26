package kvansipto.telegram.microservice.services.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.UserState;
import kvansipto.telegram.microservice.services.UserStateFactory;
import kvansipto.telegram.microservice.services.UserStateService;
import kvansipto.telegram.microservice.services.dto.AnswerData;
import kvansipto.telegram.microservice.services.dto.AnswerDto;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.EditMessageWrapper;
import kvansipto.telegram.microservice.utils.KeyboardMarkupUtil;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddDateForExerciseResultCommandTest {

  @InjectMocks
  private AddDateForExerciseResultCommand addDateForExerciseResultCommand;

  @Mock
  InlineKeyboardMarkup inlineKeyboardMarkup;
  @Mock
  RestToExercises restToExercises;
  @Mock
  UserState userState;
  @Mock
  UserStateService userStateService;
  @Mock
  UserStateFactory userStateFactory;

  static MockedStatic<AnswerData> mockedStaticAnswerData;
  private static Update update;

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
    answerDto.setButtonCode(ExerciseCommand.ADD_EXERCISE_RESULT_TEXT);
    answerDto.setHiddenText("exerciseName");
    mockedStaticAnswerData = mockStatic(AnswerData.class);
    mockedStaticAnswerData.when(() -> AnswerData.deserialize("mockData")).thenReturn(answerDto);
  }

  @AfterAll
  static void tearDown() {
    mockedStaticAnswerData.close();
  }

  @Test
  void supports_shouldReturnTrue_whenUpdateHasCallBackQueryWithAddExerciseResultButtonCode() {
    boolean result = addDateForExerciseResultCommand.supports(update);
    assertTrue(result);
  }

  @Test
  void process_shouldReturnEditMessageWrapper() {
    // Arrange
    when(userStateFactory.createUserSession(anyString())).thenReturn(userState);
    ExerciseDto exercise = ExerciseDto.builder().build();
    when(restToExercises.getExerciseByName("exerciseName")).thenReturn(exercise);

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");
    List<AnswerDto> expectedAnswers = new ArrayList<>();
    expectedAnswers.add(new AnswerDto("Сегодня", "ADD_DATE_EXERCISE_RESULT_", LocalDate.now().format(dtf)));
    expectedAnswers.add(new AnswerDto("Вчера", "ADD_DATE_EXERCISE_RESULT_", LocalDate.now().minusDays(1).format(dtf)));
    for (int i = 2; i < DayOfWeek.values().length; i++) {
      String date = LocalDate.now().minusDays(i).format(dtf);
      expectedAnswers.add(new AnswerDto(date, "ADD_DATE_EXERCISE_RESULT_", date));
    }
    try (MockedStatic<KeyboardMarkupUtil> mockedStaticKeyboardMarkupUtil = mockStatic(KeyboardMarkupUtil.class)) {
      mockedStaticKeyboardMarkupUtil.when(() -> KeyboardMarkupUtil.createRows(anyList(), anyInt()))
          .thenReturn(inlineKeyboardMarkup);

      // Act
      BotApiMethodInterface result = addDateForExerciseResultCommand.process(update);

      // Assert
      assertThat(result).isInstanceOf(EditMessageWrapper.class);
      EditMessageWrapper editMessageWrapper = (EditMessageWrapper) result;

      assertThat(editMessageWrapper.getChatId()).isEqualTo("123456");
      assertThat(editMessageWrapper.getText()).isEqualTo(
          AddDateForExerciseResultCommand.ADD_DATE_FOR_EXERCISE_RESULT_TEXT);
      assertThat(editMessageWrapper.getReplyMarkup()).isEqualTo(inlineKeyboardMarkup);

      // Verify user state changes
      verify(userState).setCurrentExercise(exercise);
      verify(userState).setCurrentState("CHOOSING DATE");
      verify(userStateService).setCurrentState("123456", userState);

      // Verify that the createRows method was called with the correct arguments
      mockedStaticKeyboardMarkupUtil.verify(() -> KeyboardMarkupUtil.createRows(eq(expectedAnswers), eq(2)), times(1));
    }
  }
}