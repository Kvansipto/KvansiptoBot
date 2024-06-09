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
  @Mock
  private Message message;
  @Mock
  private Update update;
  @Mock
  private Chat chat;

  @BeforeEach
  void setUp() {
    when(update.getMessage()).thenReturn(message);
    when(message.getChat()).thenReturn(chat);
    when(message.getChatId()).thenReturn(123456L);
    when(chat.getFirstName()).thenReturn("John");
    when(chat.getUserName()).thenReturn("johndoe");
    when(chat.getLastName()).thenReturn("Doe");
  }

  @Test
  void supports_shouldReturnTrue_whenUpdateHasMessageWithStartCommandText() {
    when(update.hasMessage()).thenReturn(true);
    when(message.getText()).thenReturn(StartCommand.START_COMMAND_TEXT);
    boolean result = startCommand.supports(update);
    assertTrue(result);
  }

  @Test
  void supports_shouldReturnFalse_whenUpdateHasMessageWithDifferentCommandText() {
    when(message.getText()).thenReturn("/differentCommand");
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