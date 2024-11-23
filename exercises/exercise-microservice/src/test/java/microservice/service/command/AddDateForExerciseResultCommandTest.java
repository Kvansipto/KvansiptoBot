package microservice.service.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
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
class AddDateForExerciseResultCommandTest {

  AddDateForExerciseResultCommand command;
  UserInputCommandEvent event;
  UpdateDto updateDto;
  Long chatId;
  UserDto user;
  UserState userState;

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

    command = new AddDateForExerciseResultCommand(kafkaTemplate, kafkaExerciseService, userStateService);

    updateDto = new UpdateDto();
    updateDto.setMessage(AnswerData.serialize(new AnswerDto("ADD_EXERCISE_RESULT", "button_code")));
    updateDto.setMessageId(42);

    event = new UserInputCommandEvent(this, chatId, updateDto);

    userState = new UserState();
    userState.setUserStateType(UserStateType.VIEWING_EXERCISE);

    lenient().when(userStateService.getCurrentState(chatId)).thenReturn(Optional.of(userState));
    lenient().when(kafkaExerciseService.sendBotApiMethod(eq(chatId), any(EditMessageWrapper.class)))
        .thenReturn(Mono.empty());
  }

  @Test
  void supports_shouldReturnTrue_whenUserStateIsViewingExerciseAndButtonCodeIsCorrect() {
    // Подготовка корректного сообщения
    String correctButtonCode = AddDateForExerciseResultCommand.ADD_DATE_EXERCISE_RESULT_TEXT;
    updateDto.setMessage(AnswerData.serialize(new AnswerDto(correctButtonCode, "ADD_RESULT")));
    event = new UserInputCommandEvent(this, chatId, updateDto);

    boolean result = command.supports(event);
    assertTrue(result, "supports() должен возвращать true для правильного состояния и кнопки");
  }


  @Test
  void supports_shouldReturnFalse_whenUserStateIsNotViewingExercise() {
    // Изменяем состояние пользователя
    userState.setUserStateType(UserStateType.WAITING_FOR_RESULT);

    // Проверяем, что команда не поддерживает данные
    boolean result = command.supports(event);
    assertFalse(result, "supports() должен возвращать false для неправильного состояния");
  }

  @Test
  void process_shouldUpdateUserStateAndSendEditMessage() {
    // Act
    command.process(event);

    // Assert: Проверяем обновление состояния пользователя
    verify(userStateService, times(1)).setCurrentState(eq(chatId), argThat(state ->
        UserStateType.CHOOSING_DATE.equals(state.getUserStateType())
    ));

    // Assert: Проверяем отправку сообщения
    verify(kafkaExerciseService, times(1)).sendBotApiMethod(eq(chatId),
        argThat(argument -> {
          if (!(argument instanceof EditMessageWrapper message)) {
            return false;
          }

          // Проверка текста сообщения
          if (!message.getText().equals(AddDateForExerciseResultCommand.ADD_DATE_FOR_EXERCISE_RESULT_TEXT)) {
            return false;
          }

          // Проверка клавиатуры
          var dtf = DateTimeFormatter.ofPattern("dd/MM");
          var expectedButtons = IntStream.range(0, 7)
              .mapToObj(i -> i < 2 ? (i == 0 ? AddDateForExerciseResultCommand.TODAY_TEXT
                  : AddDateForExerciseResultCommand.YESTERDAY_TEXT)
                  : LocalDate.now().minusDays(i).format(dtf))
              .map(date -> new AnswerDto(date, AddDateForExerciseResultCommand.ADD_DATE_EXERCISE_RESULT_TEXT))
              .map(AnswerData::serialize)
              .toList();

          var actualButtons = ((EditMessageWrapper) argument).getReplyMarkup().getKeyboard().stream()
              .flatMap(List::stream)
              .map(button -> AnswerData.serialize(
                  new AnswerDto(button.getText(), AddDateForExerciseResultCommand.ADD_DATE_EXERCISE_RESULT_TEXT)))
              .toList();

          return expectedButtons.equals(actualButtons);
        })
    );
  }
  @Test
  void answerDataDeserialize_shouldReturnCorrectAnswerDto() {
    String serialized = AnswerData.serialize(new AnswerDto("ADD_EXERCISE_RESULT", "ADD_DATE_EXERCISE_RESULT"));
    AnswerDto deserialized = AnswerData.deserialize(serialized);

    assertEquals("ADD_EXERCISE_RESULT", deserialized.getButtonText());
    assertEquals("ADD_DATE_EXERCISE_RESULT", deserialized.getButtonCode());
  }
}
