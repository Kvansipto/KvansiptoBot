package kvansipto.telegram.microservice.services.command.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vdurmont.emoji.EmojiParser;
import java.util.stream.Stream;
import kvansipto.exercise.dto.UserDto;
import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.SendMessageWrapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StartCommandTest {

  @InjectMocks
  private StartCommand startCommand;
  @Mock
  private RestToExercises restToExercises;

  private static Update update;
  private static Message message;

  @BeforeAll
  static void setUp() {
    Chat chat = new Chat();
    chat.setId(123456L);
    chat.setFirstName("John");
    chat.setLastName("Doe");
    chat.setUserName("johndoe");
    message = new Message();
    message.setChat(chat);
    message.setText(StartCommand.START_COMMAND_TEXT);
    update = new Update();
    update.setMessage(message);
  }

  @Test
  void supports_shouldReturnTrue_whenUpdateHasMessageWithStartCommandText() {
    boolean result = startCommand.supports(update);
    assertTrue(result);
  }

  @Test
  void supports_shouldReturnFalse_whenUpdateHasMessageWithDifferentCommandText() {
    message.setText("/differentCommand");
    update.setMessage(message);
    boolean result = startCommand.supports(update);
    assertFalse(result);
  }

  @ParameterizedTest
  @MethodSource("provideParametersForUserExistTest")
  void process_shouldSaveUserAndReturnCorrectMessage_whenUserNotExists(boolean userExists, int saveUserTimes) {
    when(restToExercises.userExists(anyLong())).thenReturn(userExists);
    BotApiMethodInterface result = startCommand.process(update);

    assertNotNull(result);
    assertInstanceOf(SendMessageWrapper.class, result);
    SendMessageWrapper sendMessageWrapper = (SendMessageWrapper) result;
    assertEquals("123456", sendMessageWrapper.getChatId());
    assertEquals(EmojiParser.parseToUnicode("Hi, John! Nice to meet you! :fire:"), sendMessageWrapper.getText());

    verify(restToExercises, times(1)).userExists(123456L);
    verify(restToExercises, times(saveUserTimes)).saveUser(any(UserDto.class));
  }

  private Stream<Arguments> provideParametersForUserExistTest() {
    return Stream.of(
        Arguments.of(false, 1),
        Arguments.of(true, 0)
    );
  }
}