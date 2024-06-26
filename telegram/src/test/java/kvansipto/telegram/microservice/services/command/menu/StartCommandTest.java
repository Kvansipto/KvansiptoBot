package kvansipto.telegram.microservice.services.command.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vdurmont.emoji.EmojiParser;
import kvansipto.exercise.dto.UserDto;
import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.SendMessageWrapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

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

  @Test
  void process_shouldSaveUserAndReturnCorrectMessage_whenUserNotExists() {
    when(restToExercises.userExists(anyString())).thenReturn(false);
    BotApiMethodInterface result = startCommand.process(update);

    assertNotNull(result);
    assertInstanceOf(SendMessageWrapper.class, result);
    SendMessageWrapper sendMessageWrapper = (SendMessageWrapper) result;
    assertEquals("123456", sendMessageWrapper.getChatId());
    assertEquals(EmojiParser.parseToUnicode("Hi, John! Nice to meet you! :fire:"), sendMessageWrapper.getText());

    verify(restToExercises, times(1)).userExists("123456");
    verify(restToExercises, times(1)).saveUser(any(UserDto.class));
  }

  @Test
  void process_shouldNotSaveUser_whenUserExists() {
    when(restToExercises.userExists(anyString())).thenReturn(true);
    BotApiMethodInterface result = startCommand.process(update);

    assertNotNull(result);
    assertInstanceOf(SendMessageWrapper.class, result);
    SendMessageWrapper sendMessageWrapper = (SendMessageWrapper) result;
    assertEquals("123456", sendMessageWrapper.getChatId());
    assertEquals(EmojiParser.parseToUnicode("Hi, John! Nice to meet you! :fire:"), sendMessageWrapper.getText());

    verify(restToExercises, times(1)).userExists("123456");
    verify(restToExercises, never()).saveUser(any(UserDto.class));
  }
}