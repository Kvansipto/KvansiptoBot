package kvansipto.telegram.microservice.services.command;

import static kvansipto.telegram.microservice.services.command.ExerciseCommand.EXERCISE_TEXT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.dto.AnswerData;
import kvansipto.telegram.microservice.services.dto.AnswerDto;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.EditMessageWrapper;
import kvansipto.telegram.microservice.utils.KeyboardMarkupUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExerciseCommandTest {

  @InjectMocks
  ExerciseCommand exerciseCommand;

  @Mock
  RestToExercises restToExercises;
  @Mock
  Update update;
  @Mock
  Message message;
  @Mock
  CallbackQuery callbackQuery;
  @Mock
  AnswerDto answerDto;
  @Mock
  ExerciseDto exercise;
  @Mock
  InlineKeyboardMarkup inlineKeyboardMarkup;

  MockedStatic<AnswerData> mockedStaticAnswerData;

  @BeforeEach
  void setUp() {
    when(update.hasCallbackQuery()).thenReturn(true);
    when(update.getCallbackQuery()).thenReturn(callbackQuery);
    when(callbackQuery.getMessage()).thenReturn(message);
    when(callbackQuery.getData()).thenReturn("mockData");
    when(message.getChatId()).thenReturn(123456L);
    when(answerDto.getButtonCode()).thenReturn("exercise");
    when(answerDto.getButtonText()).thenReturn("someButtonText");
    mockedStaticAnswerData = mockStatic(AnswerData.class);
    mockedStaticAnswerData.when(() -> AnswerData.deserialize("mockData")).thenReturn(answerDto);
  }

  @AfterEach
  void tearDown() {
    mockedStaticAnswerData.close();
  }

  @Test
  void supports_shouldReturnTrue_whenUpdateHasCallBackQueryWithExerciseButtonCode() {
    boolean result = exerciseCommand.supports(update);
    assertTrue(result);
  }

  @Test
  void process_shouldReturnEditMessageWrapper() {
    // Arrange
    when(exercise.getName()).thenReturn("exerciseName");
    when(exercise.getDescription()).thenReturn("exerciseDescription");
    when(exercise.getVideoUrl()).thenReturn("exerciseVideoUrl");
    when(restToExercises.getExerciseByName(anyString())).thenReturn(exercise);
    try (
        MockedStatic<KeyboardMarkupUtil> mockedStaticKeyboard = mockStatic(KeyboardMarkupUtil.class)) {
      mockedStaticKeyboard.when(() -> KeyboardMarkupUtil.createRows(anyList(), anyInt()))
          .thenReturn(inlineKeyboardMarkup);
      List<AnswerDto> expectedAnswerDtoList = new ArrayList<>();
      expectedAnswerDtoList.add(new AnswerDto("SHOW_RESULT_HISTORY", "E_HISTORY", "exerciseName"));
      expectedAnswerDtoList.add(new AnswerDto("ADD_RESULT", "ADD_RESULT", "exerciseName"));

      // Act
      BotApiMethodInterface result = exerciseCommand.process(update);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result).isInstanceOf(EditMessageWrapper.class);
      EditMessageWrapper editMessageWrapper = (EditMessageWrapper) result;
      assertThat(editMessageWrapper.getChatId()).isEqualTo("123456");
      assertThat(editMessageWrapper.getReplyMarkup()).isNotNull();
      assertThat(editMessageWrapper.getText()).isEqualTo((String.format("%s%n" + EXERCISE_TEXT,
          exercise.getDescription(), exercise.getVideoUrl())));
      assertThat(editMessageWrapper.getParseMode()).isEqualTo("MarkdownV2");
      assertThat(editMessageWrapper.getDisableWebPagePreview()).isEqualTo(false);

      // Verify
      verify(restToExercises, times(1)).getExerciseByName(anyString());
      mockedStaticAnswerData.verify(() -> AnswerData.deserialize(anyString()), times(1));
      mockedStaticKeyboard.verify(() -> KeyboardMarkupUtil.createRows(expectedAnswerDtoList, 1), times(1));
    }
  }
}