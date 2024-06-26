package kvansipto.telegram.microservice.services.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.dto.AnswerData;
import kvansipto.telegram.microservice.services.dto.AnswerDto;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.EditMessageWrapper;
import kvansipto.telegram.microservice.utils.KeyboardMarkupUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MuscleCommandTest {

  @InjectMocks
  MuscleCommand muscleCommand;
  @Mock
  RestToExercises restToExercises;

  private static Update update;
  private static AnswerDto answerDto;
  private MockedStatic<AnswerData> mockedStaticAnswerData;

  @BeforeAll
  static void setUp() {
    Chat chat = new Chat();
    chat.setId(123456L);
    Message message = new Message();
    message.setChat(chat);
    CallbackQuery callbackQuery = new CallbackQuery();
    callbackQuery.setData("mockData");
    callbackQuery.setMessage(message);
    update = new Update();
    update.setCallbackQuery(callbackQuery);
    answerDto = new AnswerDto();
    answerDto.setButtonCode("muscle_group");
    answerDto.setButtonText("core");
  }

  @BeforeEach
  void setUpMocks() {
    mockedStaticAnswerData = mockStatic(AnswerData.class);
    mockedStaticAnswerData.when(() -> AnswerData.deserialize("mockData")).thenReturn(answerDto);
  }

  @AfterEach
  void tearDown() {
    mockedStaticAnswerData.close();
  }

  @Test
  void supports_shouldReturnTrue_whenUpdateHasCallBackQueryWithMuscleGroupButtonCode() {
    boolean result = muscleCommand.supports(update);
    assertTrue(result);
  }

  @Test
  void process_shouldReturnEditMessageWrapper() {
    // Arrange
    List<ExerciseDto> exerciseDtos = List.of(
        ExerciseDto.builder().name("first").build(),
        ExerciseDto.builder().name("second").build());
    List<AnswerDto> answerDtoList = exerciseDtos.stream()
        .map(e -> new AnswerDto(e.getName(), "exercise"))
        .toList();
    when(restToExercises.getExercisesByMuscleGroup(anyString())).thenReturn(exerciseDtos);

    try (
        MockedStatic<KeyboardMarkupUtil> mockedStaticKeyboard = mockStatic(KeyboardMarkupUtil.class)) {
      InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
      mockedStaticKeyboard.when(() -> KeyboardMarkupUtil.createRows(anyList(), anyInt()))
          .thenReturn(inlineKeyboardMarkup);

      // Act
      BotApiMethodInterface result = muscleCommand.process(update);

      // Assert
      assertNotNull(result);
      assertInstanceOf(EditMessageWrapper.class, result);
      EditMessageWrapper editMessageWrapper = (EditMessageWrapper) result;
      assertEquals("123456", editMessageWrapper.getChatId());
      assertNotNull(editMessageWrapper.getReplyMarkup());
      assertEquals("Выберите упражнение", editMessageWrapper.getText());

      // Verify
      verify(restToExercises, times(1)).getExercisesByMuscleGroup(anyString());
      mockedStaticAnswerData.verify(() -> AnswerData.deserialize(anyString()), times(1));
      mockedStaticKeyboard.verify(() -> KeyboardMarkupUtil.createRows(answerDtoList, 1), times(1));
    }
  }
}