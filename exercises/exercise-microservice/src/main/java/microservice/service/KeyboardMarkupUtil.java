package microservice.service;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import microservice.service.dto.AnswerData;
import microservice.service.dto.AnswerDto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

//TODO AOP?
@UtilityClass
public class KeyboardMarkupUtil {

  public InlineKeyboardMarkup createRows(List<AnswerDto> answers, int columns) {
    var markupInline = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> rows = new ArrayList<>();

    var numRows = (int) Math.ceil((double) answers.size() / columns);

    for (int row = 0; row < numRows; row++) {
      var start = row * columns;
      var end = Math.min(start + columns, answers.size());

      List<InlineKeyboardButton> currentRow = new ArrayList<>();
      for (int j = start; j < end; j++) {
        var answer = answers.get(j);
        var button = InlineKeyboardButton.builder()
            .text(answer.getButtonText())
            .callbackData(AnswerData.serialize(answer))
            .build();
        currentRow.add(button);
      }
      rows.add(currentRow);
    }
    markupInline.setKeyboard(rows);
    return markupInline;
  }
}
