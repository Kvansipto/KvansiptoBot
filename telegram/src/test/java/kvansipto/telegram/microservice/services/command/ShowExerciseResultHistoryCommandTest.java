package kvansipto.telegram.microservice.services.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.dto.AnswerData;
import kvansipto.telegram.microservice.services.dto.AnswerDto;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.EditMessageWrapper;
import kvansipto.telegram.microservice.utils.TableImageService;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShowExerciseResultHistoryCommandTest {

  @InjectMocks
  private ShowExerciseResultHistoryCommand showExerciseResultHistoryCommand;

  @Mock
  private RestToExercises restToExercises;

  private MockedStatic<AnswerData> mockedStaticAnswerData;
  private MockedStatic<TableImageService> mockedStaticTableImage;
  private static Update update;
  private static AnswerDto answerDto;
  private static ExerciseDto exercise;

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
    answerDto.setButtonCode(ExerciseCommand.SHOW_EXERCISE_RESULT_HISTORY);
    answerDto.setHiddenText("exerciseName");
    exercise = ExerciseDto.builder()
        .name("exerciseName")
        .description("exerciseDescription")
        .videoUrl("exerciseVideoUrl")
        .build();
  }

  @BeforeEach
  void setUpMocks() {
    when(restToExercises.getExerciseByName(anyString())).thenReturn(exercise);
    mockedStaticAnswerData = mockStatic(AnswerData.class);
    mockedStaticAnswerData.when(() -> AnswerData.deserialize("mockData")).thenReturn(answerDto);
    mockedStaticTableImage = mockStatic(TableImageService.class);
  }

  @AfterEach
  void tearDown() {
    mockedStaticAnswerData.close();
    mockedStaticTableImage.close();
  }

  @Test
  void supports_shouldReturnTrue_whenUpdateHasCallbackQueryWithShowExerciseResultHistoryButtonCode()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method method = ShowExerciseResultHistoryCommand.class.getDeclaredMethod("supports", Update.class);
    method.setAccessible(true);
    boolean result = (boolean) method.invoke(showExerciseResultHistoryCommand, update);
    assertTrue(result);
  }

  @Test
  void process_shouldReturnSendMessageWrapper_whenExerciseResultsAreRequested() {
    // Arrange
    when(restToExercises.getExerciseByName(anyString())).thenReturn(exercise);

    // Act
    BotApiMethodInterface result = showExerciseResultHistoryCommand.process(update);

    // Assert
    assertThat(result).isInstanceOf(EditMessageWrapper.class);
    EditMessageWrapper editMessageWrapper = (EditMessageWrapper) result;
    assertThat(editMessageWrapper.getChatId()).isEqualTo("123456");
    assertThat(editMessageWrapper.getText()).isEqualTo(String.format(
        "Загрузка данных по упражнению %s, пожалуйста, подождите...", "exerciseName"));
  }
}
