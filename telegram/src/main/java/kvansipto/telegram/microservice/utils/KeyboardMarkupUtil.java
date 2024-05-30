package kvansipto.telegram.microservice.utils;

import java.util.ArrayList;
import java.util.List;
import kvansipto.telegram.microservice.services.dto.AnswerData;
import kvansipto.telegram.microservice.services.dto.AnswerDto;
import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@UtilityClass
public class KeyboardMarkupUtil {

  public InlineKeyboardMarkup createRows(List<AnswerDto> answers, int columns) {
    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> rows = new ArrayList<>();

    int numRows = (int) Math.ceil((double) answers.size() / columns);

    for (int row = 0; row < numRows; row++) {
      int start = row * columns;
      int end = Math.min(start + columns, answers.size());

      List<InlineKeyboardButton> currentRow = new ArrayList<>();
      for (int j = start; j < end; j++) {
        AnswerDto answer = answers.get(j);
        InlineKeyboardButton button = InlineKeyboardButton.builder()
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
