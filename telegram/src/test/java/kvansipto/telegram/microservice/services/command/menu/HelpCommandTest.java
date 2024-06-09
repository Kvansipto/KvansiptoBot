package kvansipto.telegram.microservice.services.command.menu;

import static kvansipto.telegram.microservice.services.TelegramBot.HELP_TEXT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import kvansipto.telegram.microservice.services.TelegramBot;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.SendMessageWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@ExtendWith(MockitoExtension.class)
class HelpCommandTest {

  @InjectMocks
  HelpCommand helpCommand;

  @Mock
  Update update;
  @Mock
  Message message;
  @Mock
  TelegramBot bot;

  @Test
  void supports_shouldReturnTrue_whenUpdateHasMessageWithHelpCommandText() {
    when(update.getMessage()).thenReturn(message);
    when(update.hasMessage()).thenReturn(true);
    when(message.getText()).thenReturn(HelpCommand.HELP_COMMAND_TEXT);
    boolean result = helpCommand.supports(update);
    assertTrue(result);
  }

  @Test
  void process_shouldReturnHelpText() {
    // Arrange
    when(update.getMessage()).thenReturn(message);
    when(message.getChatId()).thenReturn(123456L);
    HELP_TEXT = "help command text";

    // Act
    BotApiMethodInterface result = helpCommand.process(update);

    // Assert
    assertNotNull(result);
    assertInstanceOf(SendMessageWrapper.class, result);
    SendMessageWrapper sendMessageWrapper = (SendMessageWrapper) result;
    assertEquals("123456", sendMessageWrapper.getChatId());
    assertEquals(HELP_TEXT, sendMessageWrapper.getText());
  }

  @Test
  void explanation_shouldReturnRightMessage() {
    String result = helpCommand.explanation();
    assertEquals("how to use this bot", result);
  }
}