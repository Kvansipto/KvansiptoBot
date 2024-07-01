package kvansipto.telegram.microservice.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import kvansipto.telegram.microservice.services.dto.AnswerData;
import kvansipto.telegram.microservice.services.dto.AnswerDto;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

class KeyboardMarkupUtilTest {

  @Test
  void createRows_shouldCreateCorrectMarkupWithExactColumns() {
    // Arrange
    List<AnswerDto> answers = new ArrayList<>();
    answers.add(new AnswerDto("button1", "code1", "hidden1"));
    answers.add(new AnswerDto("button2", "code2", "hidden2"));

    // Act
    InlineKeyboardMarkup result = KeyboardMarkupUtil.createRows(answers, 2);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getKeyboard()).hasSize(1);

    List<InlineKeyboardButton> row = result.getKeyboard().get(0);
    assertThat(row).hasSize(2);

    assertThat(row.get(0).getText()).isEqualTo("button1");
    assertThat(row.get(0).getCallbackData()).isEqualTo(AnswerData.serialize(answers.get(0)));

    assertThat(row.get(1).getText()).isEqualTo("button2");
    assertThat(row.get(1).getCallbackData()).isEqualTo(AnswerData.serialize(answers.get(1)));
  }

  @Test
  void createRows_shouldCreateCorrectMarkupWithMultipleRows() {
    // Arrange
    List<AnswerDto> answers = new ArrayList<>();
    answers.add(new AnswerDto("button1", "code1", "hidden1"));
    answers.add(new AnswerDto("button2", "code2", "hidden2"));
    answers.add(new AnswerDto("button3", "code3", "hidden3"));

    // Act
    InlineKeyboardMarkup result = KeyboardMarkupUtil.createRows(answers, 2);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getKeyboard()).hasSize(2);

    List<InlineKeyboardButton> firstRow = result.getKeyboard().get(0);
    assertThat(firstRow).hasSize(2);

    assertThat(firstRow.get(0).getText()).isEqualTo("button1");
    assertThat(firstRow.get(0).getCallbackData()).isEqualTo(AnswerData.serialize(answers.get(0)));

    assertThat(firstRow.get(1).getText()).isEqualTo("button2");
    assertThat(firstRow.get(1).getCallbackData()).isEqualTo(AnswerData.serialize(answers.get(1)));

    List<InlineKeyboardButton> secondRow = result.getKeyboard().get(1);
    assertThat(secondRow).hasSize(1);

    assertThat(secondRow.get(0).getText()).isEqualTo("button3");
    assertThat(secondRow.get(0).getCallbackData()).isEqualTo(AnswerData.serialize(answers.get(2)));
  }

  @Test
  void createRows_shouldCreateCorrectMarkupWithLessColumns() {
    // Arrange
    List<AnswerDto> answers = new ArrayList<>();
    answers.add(new AnswerDto("button1", "code1", "hidden1"));
    answers.add(new AnswerDto("button2", "code2", "hidden2"));
    answers.add(new AnswerDto("button3", "code3", "hidden3"));

    // Act
    InlineKeyboardMarkup result = KeyboardMarkupUtil.createRows(answers, 1);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getKeyboard()).hasSize(3);

    List<InlineKeyboardButton> firstRow = result.getKeyboard().get(0);
    assertThat(firstRow).hasSize(1);

    assertThat(firstRow.get(0).getText()).isEqualTo("button1");
    assertThat(firstRow.get(0).getCallbackData()).isEqualTo(AnswerData.serialize(answers.get(0)));

    List<InlineKeyboardButton> secondRow = result.getKeyboard().get(1);
    assertThat(secondRow).hasSize(1);

    assertThat(secondRow.get(0).getText()).isEqualTo("button2");
    assertThat(secondRow.get(0).getCallbackData()).isEqualTo(AnswerData.serialize(answers.get(1)));

    List<InlineKeyboardButton> thirdRow = result.getKeyboard().get(2);
    assertThat(thirdRow).hasSize(1);

    assertThat(thirdRow.get(0).getText()).isEqualTo("button3");
    assertThat(thirdRow.get(0).getCallbackData()).isEqualTo(AnswerData.serialize(answers.get(2)));
  }
}
