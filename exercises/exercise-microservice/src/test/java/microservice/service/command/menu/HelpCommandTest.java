package microservice.service.command.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import kvansipto.exercise.dto.UpdateDto;
import kvansipto.exercise.dto.UserDto;
import kvansipto.exercise.wrapper.BotApiMethodInterface;
import kvansipto.exercise.wrapper.SendMessageWrapper;
import microservice.service.KafkaExerciseService;
import microservice.service.event.UserInputCommandEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class HelpCommandTest {

  HelpCommand helpCommand;
  UserInputCommandEvent userInputCommandEvent;
  UpdateDto updateDto;
  Long chatId;
  String helpText;

  @Mock
  private KafkaExerciseService kafkaExerciseService;
  @Mock
  KafkaTemplate<Long, BotApiMethodInterface> kafkaTemplate;

  @BeforeEach
  void setUp() {
    chatId = 100L;
    helpCommand = new HelpCommand(kafkaTemplate, kafkaExerciseService);
    helpText = "Help Text";
    helpCommand.setHelpText(helpText);

    lenient().when(kafkaExerciseService.sendBotApiMethod(eq(chatId), any(SendMessageWrapper.class)))
        .thenReturn(Mono.empty());

    updateDto = new UpdateDto("/help", 1, new UserDto());
    userInputCommandEvent = new UserInputCommandEvent(this, chatId, updateDto);
  }

  @Test
  void supports_shouldReturnTrue_whenUpdateHasMessageWithHelpCommandText() {
    boolean result = helpCommand.supports(userInputCommandEvent);
    assertTrue(result);
  }

  @Test
  void process_shouldInvokeSendBotApiMethodWithSendMessageWrapperWithHelpText() {
    // Act
    helpCommand.process(userInputCommandEvent);

    // Assert
    verify(kafkaExerciseService, times(1)).sendBotApiMethod(eq(100L),
        argThat(argument -> {
          if (!(argument instanceof SendMessageWrapper message)) {
            return false;
          }
          return helpText.equals(message.getText()) &&
              message.getChatId().equals(chatId.toString());
        })
    );
  }

  @Test
  void explanation_shouldReturnRightMessage() {
    String result = helpCommand.explanation();
    assertEquals("how to use this bot", result);
  }
}