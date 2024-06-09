package kvansipto.telegram.microservice.services.command.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.dto.AnswerDto;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.SendMessageWrapper;
import kvansipto.telegram.microservice.utils.KeyboardMarkupUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@ExtendWith(MockitoExtension.class)
class MuscleGroupCommandTest {

  @InjectMocks
  private MuscleGroupCommand muscleGroupCommand;

  @Mock
  private Update update;
  @Mock
  private Message message;
  @Mock
  private RestToExercises restToExercises;

  @Test
  void supports_shouldReturnTrue_whenUpdateHasMessageWithRightCommandText() {
    when(update.getMessage()).thenReturn(message);
    when(update.hasMessage()).thenReturn(true);
    when(message.getText()).thenReturn("/exercise_info");
    boolean result = muscleGroupCommand.supports(update);
    assertTrue(result);
  }

  @Test
  void process_shouldCallGetMuscleGroupsAndCreateRows() {
    // Arrange
    when(update.getMessage()).thenReturn(message);
    when(message.getChatId()).thenReturn(123456L);

    List<String> muscleGroups = List.of("Chest", "Back", "Legs", "Core");
    List<AnswerDto> answerDtoList = muscleGroups.stream()
        .map(m -> new AnswerDto(m, "muscle_group"))
        .toList();

    when(restToExercises.getMuscleGroups()).thenReturn(muscleGroups);

    // Act
    try (MockedStatic<KeyboardMarkupUtil> mockedStatic = mockStatic(KeyboardMarkupUtil.class)) {
      InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
      mockedStatic.when(() -> KeyboardMarkupUtil.createRows(anyList(), anyInt())).thenReturn(inlineKeyboardMarkup);

      BotApiMethodInterface result = muscleGroupCommand.process(update);

      // Assert
      assertNotNull(result);
      assertInstanceOf(SendMessageWrapper.class, result);
      SendMessageWrapper sendMessageWrapper = (SendMessageWrapper) result;
      assertEquals("123456", sendMessageWrapper.getChatId());
      assertEquals("Выберите группу мышц", sendMessageWrapper.getText());
      assertNotNull(sendMessageWrapper.getReplyMarkup());
      assertInstanceOf(InlineKeyboardMarkup.class, sendMessageWrapper.getReplyMarkup());

      // Verify interactions
      verify(restToExercises, times(1)).getMuscleGroups();
      mockedStatic.verify(() -> KeyboardMarkupUtil.createRows(answerDtoList, 2), times(1));
    }
  }

  @Test
  void explanation_shouldReturnRightMessage() {
    String result = muscleGroupCommand.explanation();
    assertEquals("to show muscle groups", result);
  }
}