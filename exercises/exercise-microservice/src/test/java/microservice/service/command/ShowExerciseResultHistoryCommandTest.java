package microservice.service.command;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.dto.UpdateDto;
import kvansipto.exercise.wrapper.BotApiMethodInterface;
import kvansipto.exercise.wrapper.BotApiMethodWrapper;
import kvansipto.exercise.wrapper.EditMessageWrapper;
import microservice.service.ExerciseResultService;
import microservice.service.KafkaExerciseService;
import microservice.service.TableImageService;
import microservice.service.dto.AnswerData;
import microservice.service.dto.AnswerDto;
import microservice.service.event.UserInputCommandEvent;
import microservice.service.user.state.UserState;
import microservice.service.user.state.UserStateService;
import microservice.service.user.state.UserStateType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ShowExerciseResultHistoryCommandTest {

  ShowExerciseResultHistoryCommand command;
  UserInputCommandEvent event;
  UpdateDto updateDto;
  Long chatId;
  UserState userState;
  ExerciseDto exerciseDto;

  private static final String EMPTY_LIST_EXERCISE_RESULT_TEXT = "Результаты по упражнению отсутствуют";
  private static final String EXERCISE_RESULT_TEXT = "Результаты по упражнению %s";
  private static final String[] HEADERS = {"Дата", "Вес (кг)", "Подходы", "Повторения", "Комментарий"};

  @Mock
  private KafkaExerciseService kafkaExerciseService;
  @Mock
  private UserStateService userStateService;
  @Mock
  private ExerciseResultService exerciseResultService;
  @Mock
  private TableImageService tableImageService;
  @Mock
  private KafkaTemplate<Long, BotApiMethodInterface> kafkaTemplate;

  @BeforeEach
  void setUp() {
    chatId = 100L;

    exerciseDto = ExerciseDto.builder()
        .name("Bench Press")
        .description("A chest exercise")
        .videoUrl("http://example.com/video")
        .imageUrl("http://example.com/image")
        .muscleGroup("Chest")
        .build();

    userState = new UserState();
    userState.setUserStateType(UserStateType.VIEWING_EXERCISE);
    userState.setCurrentExercise(exerciseDto);

    command = new ShowExerciseResultHistoryCommand(kafkaTemplate, kafkaExerciseService, tableImageService, userStateService, exerciseResultService);

    updateDto = new UpdateDto();
    updateDto.setMessage(AnswerData.serialize(new AnswerDto("Bench Press", ExerciseCommand.SHOW_EXERCISE_RESULT_HISTORY)));
    updateDto.setMessageId(42);

    event = new UserInputCommandEvent(this, chatId, updateDto);

    lenient().when(userStateService.getCurrentState(chatId)).thenReturn(Optional.of(userState));
    lenient().when(kafkaExerciseService.sendBotApiMethod(eq(chatId), any(BotApiMethodWrapper.class))).thenReturn(Mono.empty());
    lenient().when(kafkaExerciseService.sendMedia(eq(chatId), anyString())).thenReturn(Mono.empty());
  }

  @Test
  void supports_shouldReturnTrue_whenUserStateIsViewingExerciseAndButtonCodeIsCorrect() {
    boolean result = command.supports(event);
    assertTrue(result, "supports() должен возвращать true для состояния VIEWING_EXERCISE и корректного кода кнопки");
  }

  @Test
  void supports_shouldReturnFalse_whenUserStateIsNotViewingExercise() {
    userState.setUserStateType(UserStateType.CHOOSING_EXERCISE);

    boolean result = command.supports(event);
    assertFalse(result, "supports() должен возвращать false для неправильного состояния");
  }

  @Test
  void process_shouldSendEmptyListMessage_whenNoExerciseResultsFound() {
    when(exerciseResultService.findExerciseResults(any())).thenReturn(Collections.emptyList());

    // Act
    command.process(event);

    // Assert: Проверяем, что отправляется сообщение об отсутствии результатов
    verify(kafkaExerciseService, times(1)).sendBotApiMethod(eq(chatId),
        argThat(argument -> {
          if (!(argument instanceof BotApiMethodWrapper wrapper)) {
            return false;
          }

          var sendMessage = (EditMessageWrapper) wrapper.getActions().get(0);
          return sendMessage.getText().equals(EMPTY_LIST_EXERCISE_RESULT_TEXT) &&
              sendMessage.getChatId().equals(chatId.toString());
        })
    );

    // Assert: Проверяем, что состояние пользователя удаляется
    verify(userStateService, times(1)).removeUserState(chatId);
  }

  @Test
  void process_shouldSendExerciseResults_whenResultsFound() {
    var results = List.of(
        ExerciseResultDto.builder()
            .date(LocalDate.now())
            .weight(50.0)
            .numberOfSets(3)
            .numberOfRepetitions(10)
            .comment("Good job")
            .build(),
        ExerciseResultDto.builder()
            .date(LocalDate.now().minusDays(1))
            .weight(55.0)
            .numberOfSets(4)
            .numberOfRepetitions(8)
            .comment("Challenging")
            .build()
    );

    when(exerciseResultService.findExerciseResults(any())).thenReturn(results);

    byte[] mockImage = new byte[]{1, 2, 3};
    when(tableImageService.drawTableImage(eq(HEADERS), any())).thenReturn(mockImage);

    // Act
    command.process(event);

    // Assert: Проверяем, что отправляется изображение с результатами
    verify(kafkaExerciseService, times(1)).sendMedia(eq(chatId), eq(Base64.getEncoder().encodeToString(mockImage)));

    // Assert: Проверяем отправку текста с названием упражнения
    verify(kafkaExerciseService, times(1)).sendBotApiMethod(eq(chatId),
        argThat(argument -> {
          if (!(argument instanceof BotApiMethodWrapper wrapper)) {
            return false;
          }

          var sendMessage = (EditMessageWrapper) wrapper.getActions().get(0);
          return sendMessage.getText().equals(String.format(EXERCISE_RESULT_TEXT, exerciseDto.getName())) &&
              sendMessage.getChatId().equals(chatId.toString());
        })
    );

    // Assert: Проверяем, что состояние пользователя удаляется
    verify(userStateService, times(1)).removeUserState(chatId);
  }
}
