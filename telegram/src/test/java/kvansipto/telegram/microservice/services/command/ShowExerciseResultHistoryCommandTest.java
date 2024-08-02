package kvansipto.telegram.microservice.services.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.dto.AnswerData;
import kvansipto.telegram.microservice.services.dto.AnswerDto;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodWrapper;
import kvansipto.telegram.microservice.services.wrapper.EditMessageWrapper;
import kvansipto.telegram.microservice.services.wrapper.SendPhotoWrapper;
import kvansipto.telegram.microservice.utils.TableImage;
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
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShowExerciseResultHistoryCommandTest {

  @InjectMocks
  private ShowExerciseResultHistoryCommand showExerciseResultHistoryCommand;

  @Mock
  private RestToExercises restToExercises;
  @Mock
  private File file;

  private MockedStatic<AnswerData> mockedStaticAnswerData;
  private MockedStatic<TableImage> mockedStaticTableImage;
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
    mockedStaticTableImage = mockStatic(TableImage.class);
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
  void process_shouldReturnEditMessageWrapper_whenExerciseResultsAreEmpty() {
    // Arrange
    List<ExerciseResultDto> emptyResults = new ArrayList<>();
    when(restToExercises.getExerciseResults(any(ExerciseDto.class), anyLong())).thenReturn(emptyResults);

    // Act
    BotApiMethodInterface result = showExerciseResultHistoryCommand.process(update);

    // Assert
    assertThat(result).isInstanceOf(EditMessageWrapper.class);
    EditMessageWrapper editMessageWrapper = (EditMessageWrapper) result;
    assertThat(editMessageWrapper.getChatId()).isEqualTo("123456");
    assertThat(editMessageWrapper.getText()).isEqualTo(String.format(
        ShowExerciseResultHistoryCommand.EMPTY_LIST_EXERCISE_RESULT_TEXT, "exerciseName"));
  }

  @Test
  void process_shouldReturnBotApiMethodWrapperWithPhoto_whenExerciseResultsArePresent() {
    // Arrange
    List<ExerciseResultDto> exerciseResults = new ArrayList<>();
    exerciseResults.add(
        ExerciseResultDto.builder()
            .date(LocalDate.now())
            .weight(60.5)
            .numberOfSets((byte) 3)
            .numberOfRepetitions((byte) (12))
            .build());
    exerciseResults.add(
        ExerciseResultDto.builder()
            .date(LocalDate.now().minusDays(1))
            .weight(62.0)
            .numberOfSets((byte) 3)
            .numberOfRepetitions((byte) (10))
            .build());

    when(restToExercises.getExerciseResults(any(ExerciseDto.class), anyLong())).thenReturn(exerciseResults);
    when(TableImage.drawTableImage(any(String[].class), any(String[][].class))).thenReturn(file);

    // Act
    BotApiMethodInterface result = showExerciseResultHistoryCommand.process(update);

    // Assert
    assertThat(result).isInstanceOf(BotApiMethodWrapper.class);
    BotApiMethodWrapper botApiMethodWrapper = (BotApiMethodWrapper) result;
    List<BotApiMethodInterface> actions = botApiMethodWrapper.getActions();

    assertThat(actions).hasSize(2);

    assertThat(actions.get(0)).isInstanceOf(EditMessageWrapper.class);
    EditMessageWrapper editMessageWrapper = (EditMessageWrapper) actions.get(0);
    assertThat(editMessageWrapper.getChatId()).isEqualTo("123456");
    assertThat(editMessageWrapper.getText()).isEqualTo(String.format(
        ShowExerciseResultHistoryCommand.EXERCISE_RESULT_HISTORY_LIST_TEXT, "exerciseName"));

    assertThat(actions.get(1)).isInstanceOf(SendPhotoWrapper.class);
    SendPhotoWrapper sendPhotoWrapper = (SendPhotoWrapper) actions.get(1);
    assertThat(sendPhotoWrapper.getChatId()).isEqualTo("123456");
    assertThat(sendPhotoWrapper.getPhoto()).isInstanceOf(InputFile.class);
  }
}
