package microservice.service.command;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.UpdateDto;
import kvansipto.exercise.dto.UserDto;
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
class ExerciseCommandTest {

  ExerciseCommand command;
  UserInputCommandEvent event;
  UpdateDto updateDto;
  Long chatId;
  UserDto user;
  UserState userState;
  ExerciseDto exerciseDto;

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
    user = new UserDto();
    user.setId(chatId);

    exerciseDto = ExerciseDto.builder()
        .name("Bench Press")
        .description("A great chest exercise")
        .videoUrl("http://example.com/video")
        .imageUrl("http://example.com/image")
        .muscleGroup("Chest")
        .build();

    userState = new UserState();
    userState.setUserStateType(UserStateType.CHOOSING_EXERCISE);

    command = new ExerciseCommand(kafkaTemplate, kafkaExerciseService, exerciseService, userStateService);

    updateDto = new UpdateDto();
    updateDto.setMessage(AnswerData.serialize(new AnswerDto("Bench Press", "exercise")));
    updateDto.setMessageId(42);

    event = new UserInputCommandEvent(this, chatId, updateDto);

    lenient().when(userStateService.getCurrentState(chatId)).thenReturn(Optional.of(userState));
    lenient().when(exerciseService.getExerciseByName("Bench Press")).thenReturn(exerciseDto);
    lenient().when(kafkaExerciseService.sendBotApiMethod(eq(chatId), any(EditMessageWrapper.class)))
        .thenReturn(Mono.empty());
  }

  @Test
  void supports_shouldReturnTrue_whenUserStateIsChoosingExerciseAndButtonCodeIsCorrect() {
    boolean result = command.supports(event);
    assertTrue(result, "supports() должен возвращать true для состояния CHOOSING_EXERCISE и корректного кода кнопки");
  }

  @Test
  void supports_shouldReturnFalse_whenUserStateIsNotChoosingExercise() {
    userState.setUserStateType(UserStateType.VIEWING_EXERCISE);

    boolean result = command.supports(event);
    assertFalse(result, "supports() должен возвращать false для неправильного состояния");
  }

  @Test
  void process_shouldUpdateUserStateAndSendEditMessage() {
    // Act
    command.process(event);

    // Assert: Проверяем обновление состояния пользователя
    verify(userStateService, times(1)).setCurrentState(eq(chatId), argThat(state ->
        UserStateType.VIEWING_EXERCISE.equals(state.getUserStateType()) &&
            "Bench Press".equals(state.getCurrentExercise().getName())
    ));

    // Assert: Проверяем отправку сообщения с клавиатурой
    verify(kafkaExerciseService, times(1)).sendBotApiMethod(eq(chatId),
        argThat(argument -> {
          if (!(argument instanceof EditMessageWrapper message)) {
            return false;
          }

          // Проверяем текст сообщения
          String expectedText = String.format("%s%nПосмотрите видео с упражнением на YouTube: [Смотреть видео](%s)",
              exerciseDto.getDescription(), exerciseDto.getVideoUrl());
          if (!message.getText().equals(expectedText)) {
            return false;
          }

          // Проверяем параметры Markdown
          if (!"MarkdownV2".equals(message.getParseMode()) || message.getDisableWebPagePreview()) {
            return false;
          }

          // Проверяем клавиатуру
          var keyboard = message.getReplyMarkup().getKeyboard();
          return keyboard.size() == 2 &&
              "SHOW_RESULT_HISTORY".equals(keyboard.get(0).get(0).getText()) &&
              "ADD_RESULT".equals(keyboard.get(1).get(0).getText());
        })
    );
  }
}
