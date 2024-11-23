package microservice.service.command.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kvansipto.exercise.dto.UpdateDto;
import kvansipto.exercise.dto.UserDto;
import kvansipto.exercise.wrapper.BotApiMethodInterface;
import kvansipto.exercise.wrapper.SendMessageWrapper;
import microservice.service.KafkaExerciseService;
import microservice.service.UserService;
import microservice.service.event.UserInputCommandEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class StartCommandTest {

  StartCommand startCommand;
  UserInputCommandEvent userInputCommandEvent;
  UpdateDto updateDto;
  Long chatId;
  UserDto user;

  @Mock
  private KafkaExerciseService kafkaExerciseService;
  @Mock
  private UserService userService;
  @Mock
  KafkaTemplate<Long, BotApiMethodInterface> kafkaTemplate;

  @BeforeEach
  void setUp() {
    chatId = 100L;
    user = new UserDto();
    user.setId(1L);
    user.setFirstName("John");

    startCommand = new StartCommand(kafkaTemplate, kafkaExerciseService, userService);

    lenient().when(kafkaExerciseService.sendBotApiMethod(eq(chatId), any(SendMessageWrapper.class)))
        .thenReturn(Mono.empty());

    updateDto = new UpdateDto("/start", 1, user);
    userInputCommandEvent = new UserInputCommandEvent(this, chatId, updateDto);
  }

  @Test
  void supports_shouldReturnTrue_whenUpdateHasMessageWithStartCommandText() {
    boolean result = startCommand.supports(userInputCommandEvent);
    assertTrue(result, "supports() должен возвращать true для команды /start");
  }

  @Test
  void process_shouldRegisterUserAndSendWelcomeMessage() {
    when(userService.exists(user.getId())).thenReturn(false);
    // Act
    startCommand.process(userInputCommandEvent);

    // Assert: проверяем, что пользователь зарегистрирован
    verify(userService, times(1)).exists(user.getId());
    verify(userService, times(1)).create(user);

    // Assert: проверяем отправку сообщения
    verify(kafkaExerciseService, times(1)).sendBotApiMethod(eq(chatId),
        argThat(argument -> {
          if (!(argument instanceof SendMessageWrapper message)) {
            return false;
          }
          return message.getText().contains("Hi, John! Nice to meet you!") &&
              message.getChatId().equals(chatId.toString());
        })
    );
  }

  @Test
  void process_shouldNotRegisterUserIfUserAlreadyExists() {
    // Настройка мока для случая, когда пользователь уже существует
    when(userService.exists(user.getId())).thenReturn(true);

    // Act
    startCommand.process(userInputCommandEvent);

    // Assert: проверяем, что пользователь не был зарегистрирован
    verify(userService, times(1)).exists(user.getId());
    verify(userService, times(0)).create(user);

    // Assert: проверяем, что сообщение всё равно отправляется
    verify(kafkaExerciseService, times(1)).sendBotApiMethod(eq(chatId),
        argThat(argument -> {
          if (!(argument instanceof SendMessageWrapper message)) {
            return false;
          }
          return message.getText().contains("Hi, John! Nice to meet you!") &&
              message.getChatId().equals(chatId.toString());
        })
    );
  }

  @Test
  void explanation_shouldReturnRightMessage() {
    String result = startCommand.explanation();
    assertEquals("to register a user", result, "explanation() должен возвращать описание команды");
  }
}