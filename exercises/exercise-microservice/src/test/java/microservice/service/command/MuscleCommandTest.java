package microservice.service.command;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.UpdateDto;
import kvansipto.exercise.wrapper.BotApiMethodInterface;
import kvansipto.exercise.wrapper.EditMessageWrapper;
import microservice.service.ExerciseService;
import microservice.service.KafkaExerciseService;
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
class MuscleCommandTest {

  MuscleCommand command;
  UserInputCommandEvent event;
  UpdateDto updateDto;
  Long chatId;
  UserState userState;

  private static final String MUSCLE_COMMAND_TEXT = "Выберите упражнение";

  @Mock
  private KafkaExerciseService kafkaExerciseService;
  @Mock
  private UserStateService userStateService;
  @Mock
  private ExerciseService exerciseService;
  @Mock
  private KafkaTemplate<Long, BotApiMethodInterface> kafkaTemplate;

  @BeforeEach
  void setUp() {
    chatId = 100L;

    userState = new UserState();
    userState.setUserStateType(UserStateType.CHOOSING_MUSCLE_GROUP);

    command = new MuscleCommand(kafkaTemplate, kafkaExerciseService, exerciseService, userStateService);

    updateDto = new UpdateDto();
    updateDto.setMessage(AnswerData.serialize(new AnswerDto("Chest", "muscle_group")));
    updateDto.setMessageId(42);

    event = new UserInputCommandEvent(this, chatId, updateDto);

    lenient().when(userStateService.getCurrentState(chatId)).thenReturn(Optional.of(userState));
    lenient().when(kafkaExerciseService.sendBotApiMethod(eq(chatId), any(EditMessageWrapper.class)))
        .thenReturn(Mono.empty());
  }

  @Test
  void supports_shouldReturnTrue_whenUserStateIsChoosingMuscleGroupAndButtonCodeIsCorrect() {
    boolean result = command.supports(event);
    assertTrue(result, "supports() должен возвращать true для состояния CHOOSING_MUSCLE_GROUP и корректного кода кнопки");
  }

  @Test
  void supports_shouldReturnFalse_whenUserStateIsNotChoosingMuscleGroup() {
    userState.setUserStateType(UserStateType.CHOOSING_EXERCISE);

    boolean result = command.supports(event);
    assertFalse(result, "supports() должен возвращать false для неправильного состояния");
  }

  @Test
  void process_shouldUpdateUserStateAndSendEditMessageWithExercises() {
    // Mock exercises for the "Chest" muscle group
    var exercises = List.of(
        ExerciseDto.builder().name("Bench Press").build(),
        ExerciseDto.builder().name("Incline Bench Press").build()
    );

    when(exerciseService.getExercisesByMuscleGroup("Chest")).thenReturn(exercises);

    // Act
    command.process(event);

    // Assert: Проверяем обновление состояния пользователя
    verify(userStateService, times(1)).setCurrentState(eq(chatId), argThat(state ->
        UserStateType.CHOOSING_EXERCISE.equals(state.getUserStateType())
    ));

    // Assert: Проверяем отправку сообщения с клавиатурой
    verify(kafkaExerciseService, times(1)).sendBotApiMethod(eq(chatId),
        argThat(argument -> {
          if (!(argument instanceof EditMessageWrapper message)) {
            return false;
          }

          // Проверяем текст сообщения
          if (!message.getText().equals(MUSCLE_COMMAND_TEXT)) {
            return false;
          }

          // Проверяем клавиатуру
          var keyboard = message.getReplyMarkup().getKeyboard();
          return keyboard.size() == 2 &&
              "Bench Press".equals(keyboard.get(0).get(0).getText()) &&
              "Incline Bench Press".equals(keyboard.get(1).get(0).getText());
        })
    );
  }
}
