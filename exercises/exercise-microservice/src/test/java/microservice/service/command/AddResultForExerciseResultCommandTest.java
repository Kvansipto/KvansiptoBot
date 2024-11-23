package microservice.service.command;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Optional;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.dto.UpdateDto;
import kvansipto.exercise.dto.UserDto;
import kvansipto.exercise.wrapper.BotApiMethodInterface;
import kvansipto.exercise.wrapper.BotApiMethodWrapper;
import kvansipto.exercise.wrapper.SendMessageWrapper;
import microservice.service.ExerciseResultService;
import microservice.service.KafkaExerciseService;
import microservice.service.UserService;
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
class AddResultForExerciseResultCommandTest {

  AddResultForExerciseResultCommand command;
  UserInputCommandEvent event;
  UpdateDto updateDto;
  Long chatId;
  UserDto user;
  UserState userState;
  ExerciseDto exerciseDto;

  private static final String SAVE_RESULT_SUCCESS_TEXT = "Результат успешно сохранен";

  @Mock
  private KafkaExerciseService kafkaExerciseService;
  @Mock
  private UserService userService;
  @Mock
  private ExerciseResultService exerciseResultService;
  @Mock
  private UserStateService userStateService;
  @Mock
  private KafkaTemplate<Long, BotApiMethodInterface> kafkaTemplate;

  @BeforeEach
  void setUp() {
    chatId = 100L;
    user = new UserDto();
    user.setId(chatId);

    exerciseDto = ExerciseDto.builder()
        .name("Bench Press")
        .description("A chest exercise")
        .videoUrl("http://example.com/video")
        .imageUrl("http://example.com/image")
        .muscleGroup("Chest")
        .build();

    userState = new UserState();
    userState.setUserStateType(UserStateType.WAITING_FOR_RESULT);
    userState.setExerciseResultDate(LocalDate.now());
    userState.setCurrentExercise(exerciseDto);

    command = new AddResultForExerciseResultCommand(kafkaTemplate, kafkaExerciseService, userService, exerciseResultService, userStateService);

    updateDto = new UpdateDto();
    updateDto.setMessage("50 3 10 Great workout!");
    event = new UserInputCommandEvent(this, chatId, updateDto);

    lenient().when(userStateService.getCurrentState(chatId)).thenReturn(Optional.of(userState));
    lenient().when(userService.getOne(chatId)).thenReturn(user);
    lenient().when(kafkaExerciseService.sendBotApiMethod(eq(chatId), any(BotApiMethodWrapper.class)))
        .thenReturn(Mono.empty());
  }

  @Test
  void supports_shouldReturnTrue_whenUserStateIsWaitingForResult() {
    boolean result = command.supports(event);
    assertTrue(result, "supports() должен возвращать true для состояния WAITING_FOR_RESULT");
  }

  @Test
  void supports_shouldReturnFalse_whenUserStateIsNotWaitingForResult() {
    userState.setUserStateType(UserStateType.CHOOSING_DATE);

    boolean result = command.supports(event);
    assertFalse(result, "supports() должен возвращать false для неправильного состояния");
  }

  @Test
  void process_shouldParseMessageAndSaveExerciseResult() {
    // Act
    command.process(event);

    // Assert: Проверяем, что результат упражнения был сохранён
    verify(exerciseResultService, times(1)).create((ExerciseResultDto) argThat(result -> {
      if (!(result instanceof ExerciseResultDto exerciseResult)) {
        return false;
      }
      return exerciseResult.getWeight() == 50.0 &&
          exerciseResult.getNumberOfSets() == 3 &&
          exerciseResult.getNumberOfRepetitions() == 10 &&
          "Great workout!".equals(exerciseResult.getComment()) &&
          exerciseResult.getUser().equals(user) &&
          "Bench Press".equals(exerciseResult.getExercise().getName()) &&
          exerciseResult.getDate().equals(LocalDate.now());
    }));

    // Assert: Проверяем удаление состояния пользователя
    verify(userStateService, times(1)).removeUserState(chatId);

    // Assert: Проверяем отправку сообщения об успешном сохранении
    verify(kafkaExerciseService, times(1)).sendBotApiMethod(eq(chatId),
        argThat(argument -> {
          if (!(argument instanceof BotApiMethodWrapper wrapper)) {
            return false;
          }

          var sendMessage = (SendMessageWrapper) wrapper.getActions().get(0);
          return sendMessage.getText().equals(SAVE_RESULT_SUCCESS_TEXT) &&
              sendMessage.getChatId().equals(chatId.toString());
        })
    );
  }


  @Test
  void process_shouldHandleMessageWithoutComment() {
    // Подготовка данных без комментария
    updateDto.setMessage("50 3 10");
    event = new UserInputCommandEvent(this, chatId, updateDto);

    // Act
    command.process(event);

    // Assert: Проверяем, что результат упражнения был сохранён
    verify(exerciseResultService, times(1)).create((ExerciseResultDto) argThat(result -> {
      if (!(result instanceof ExerciseResultDto exerciseResult)) {
        return false;
      }
      return exerciseResult.getWeight() == 50.0 &&
          exerciseResult.getNumberOfSets() == 3 &&
          exerciseResult.getNumberOfRepetitions() == 10 &&
          exerciseResult.getComment() == null &&
          "Chest".equals(exerciseResult.getExercise().getMuscleGroup());
    }));

    // Assert: Проверяем удаление состояния пользователя
    verify(userStateService, times(1)).removeUserState(chatId);

    // Assert: Проверяем отправку сообщения об успешном сохранении
    verify(kafkaExerciseService, times(1)).sendBotApiMethod(eq(chatId),
        argThat(argument -> {
          if (!(argument instanceof BotApiMethodWrapper wrapper)) {
            return false;
          }

          var sendMessage = (SendMessageWrapper) wrapper.getActions().get(0);
          return sendMessage.getText().equals(SAVE_RESULT_SUCCESS_TEXT) &&
              sendMessage.getChatId().equals(chatId.toString());
        })
    );
  }
}
