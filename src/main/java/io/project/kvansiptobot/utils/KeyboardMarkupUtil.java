package io.project.kvansiptobot.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@UtilityClass
public class KeyboardMarkupUtil {

  //TODO Это все переделается при переходе на AnswerData + AnswerDto
  public InlineKeyboardMarkup generateInlineKeyboardMarkup(Iterable<String> data) {
    return generateInlineKeyboardMarkup(data, 1);
  }

  public InlineKeyboardMarkup generateInlineKeyboardMarkup(Iterable<String> data, int columnAmount) {
    Map<String, String> dataMap = new HashMap<>();
    data.forEach(e -> dataMap.put(e, e));
    return generateInlineKeyboardMarkup(dataMap, columnAmount);
  }

  public InlineKeyboardMarkup generateInlineKeyboardMarkup(Map<String, String> data) {
    return generateInlineKeyboardMarkup(data, 1);
  }

  public InlineKeyboardMarkup generateInlineKeyboardMarkup(Map<String, String> data, int columnAmount) {
    if (columnAmount <= 1) {
      return generateSingleColumnMarkup(data);
    }

    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
    List<InlineKeyboardButton> row = new ArrayList<>();
    int i = 1;

    for (Map.Entry<String, String> entry : data.entrySet()) {
      InlineKeyboardButton inlineKeyboardButton = createButton(entry.getKey(), entry.getValue());
      row.add(inlineKeyboardButton);
      if (i % columnAmount == 0) {
        rows.add(new ArrayList<>(row));
        row.clear();
      }
      i++;
    }

    if (!row.isEmpty()) {
      rows.add(row);
    }

    inlineKeyboardMarkup.setKeyboard(rows);
    return inlineKeyboardMarkup;
  }

  private InlineKeyboardMarkup generateSingleColumnMarkup(Map<String, String> data) {
    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> rows = new ArrayList<>();

    data.forEach((k, v) -> {
      List<InlineKeyboardButton> row = new ArrayList<>();
      row.add(createButton(k, v));
      rows.add(row);
    });

    inlineKeyboardMarkup.setKeyboard(rows);
    return inlineKeyboardMarkup;
  }

  private InlineKeyboardButton createButton(String text, String callbackData) {
    InlineKeyboardButton button = new InlineKeyboardButton();
    button.setText(text);
    button.setCallbackData(callbackData);
    return button;
  }
}
