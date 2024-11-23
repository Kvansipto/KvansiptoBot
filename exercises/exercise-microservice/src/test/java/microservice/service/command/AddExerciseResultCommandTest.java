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
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import kvansipto.exercise.dto.UpdateDto;
import kvansipto.exercise.dto.UserDto;
import kvansipto.exercise.wrapper.BotApiMethodInterface;
import kvansipto.exercise.wrapper.EditMessageWrapper;
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
class AddExerciseResultCommandTest {

  AddExerciseResultCommand command;
  UserInputCommandEvent event;
  UpdateDto updateDto;
  Long chatId;
  UserDto user;
  UserState userState;
  private final String ADD_EXERCISE_RESULT_TEXT =
      "Введите результат в формате:\n[(Вес в кг) (количество подходов) (количество повторений)]\n\nПример сообщения: 12.5 8 15";

  @Mock
  private KafkaExerciseService kafkaExerciseService;
  @Mock
  private UserStateService userStateService;
  @Mock
  private KafkaTemplate<Long, BotApiMethodInterface> kafkaTemplate;

  @BeforeEach
  void setUp() {
    chatId = 100L;
    user = new UserDto();
    user.setId(1L);

    command = new AddExerciseResultCommand(kafkaTemplate, kafkaExerciseService, userStateService);

    updateDto = new UpdateDto();
    updateDto.setMessage(
        AnswerData.serialize(new AnswerDto("Сегодня", AddDateForExerciseResultCommand.ADD_DATE_EXERCISE_RESULT_TEXT)));
    updateDto.setMessageId(42);

    event = new UserInputCommandEvent(this, chatId, updateDto);

    userState = new UserState();
    userState.setUserStateType(UserStateType.CHOOSING_DATE);

    lenient().when(userStateService.getCurrentState(chatId)).thenReturn(Optional.of(userState));
    lenient().when(kafkaExerciseService.sendBotApiMethod(eq(chatId), any(EditMessageWrapper.class)))
        .thenReturn(Mono.empty());
  }

  @Test
  void supports_shouldReturnTrue_whenUserStateIsChoosingDateAndButtonCodeIsCorrect() {
    boolean result = command.supports(event);
    assertTrue(result, "supports() должен возвращать true для состояния CHOOSING_DATE и корректного кода кнопки");
  }

  @Test
  void supports_shouldReturnFalse_whenUserStateIsNotChoosingDate() {
    userState.setUserStateType(UserStateType.VIEWING_EXERCISE);

    boolean result = command.supports(event);
    assertFalse(result, "supports() должен возвращать false для неправильного состояния");
  }

  @Test
  void process_shouldUpdateUserStateAndSendEditMessageForToday() {
    // Act
    command.process(event);

    // Assert: Проверяем обновление состояния пользователя
    verify(userStateService, times(1)).setCurrentState(eq(chatId), argThat(state ->
        UserStateType.WAITING_FOR_RESULT.equals(state.getUserStateType()) &&
            LocalDate.now().equals(state.getExerciseResultDate())
    ));

    // Assert: Проверяем отправку сообщения
    verify(kafkaExerciseService, times(1)).sendBotApiMethod(eq(chatId),
        argThat(argument -> {
          if (!(argument instanceof EditMessageWrapper message)) {
            return false;
          }
          return message.getText().equals(ADD_EXERCISE_RESULT_TEXT);
        })
    );
  }

  @Test
  void process_shouldUpdateUserStateAndSendEditMessageForYesterday() {
    // Подготовка данных для "Вчера"
    updateDto.setMessage(
        AnswerData.serialize(new AnswerDto("Вчера", AddDateForExerciseResultCommand.ADD_DATE_EXERCISE_RESULT_TEXT)));
    event = new UserInputCommandEvent(this, chatId, updateDto);

    // Act
    command.process(event);

    // Assert: Проверяем обновление состояния пользователя
    verify(userStateService, times(1)).setCurrentState(eq(chatId), argThat(state ->
        UserStateType.WAITING_FOR_RESULT.equals(state.getUserStateType()) &&
            LocalDate.now().minusDays(1).equals(state.getExerciseResultDate())
    ));

    // Assert: Проверяем отправку сообщения
    verify(kafkaExerciseService, times(1)).sendBotApiMethod(eq(chatId),
        argThat(argument -> {
          if (!(argument instanceof EditMessageWrapper message)) {
            return false;
          }

          return message.getText().equals(ADD_EXERCISE_RESULT_TEXT);
        })
    );
  }

  @Test
  void process_shouldUpdateUserStateAndSendEditMessageForCustomDate() {
    // Подготовка данных для произвольной даты
    String customDate = LocalDate.now().minusDays(5).format(DateTimeFormatter.ofPattern("dd/MM"));
    updateDto.setMessage(
        AnswerData.serialize(new AnswerDto(customDate, AddDateForExerciseResultCommand.ADD_DATE_EXERCISE_RESULT_TEXT)));
    event = new UserInputCommandEvent(this, chatId, updateDto);

    // Act
    command.process(event);

    // Assert: Проверяем обновление состояния пользователя
    verify(userStateService, times(1)).setCurrentState(eq(chatId), argThat(state ->
        UserStateType.WAITING_FOR_RESULT.equals(state.getUserStateType()) &&
            LocalDate.now().minusDays(5).equals(state.getExerciseResultDate())
    ));

    // Assert: Проверяем отправку сообщения
    verify(kafkaExerciseService, times(1)).sendBotApiMethod(eq(chatId),
        argThat(argument -> {
          if (!(argument instanceof EditMessageWrapper message)) {
            return false;
          }

          return message.getText().equals(ADD_EXERCISE_RESULT_TEXT);
        })
    );
  }
}
