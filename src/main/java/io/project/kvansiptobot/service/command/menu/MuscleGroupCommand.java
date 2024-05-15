package io.project.kvansiptobot.service.command.menu;

import io.project.kvansiptobot.model.MuscleGroup;
import io.project.kvansiptobot.service.wrapper.BotApiMethodInterface;
import io.project.kvansiptobot.service.wrapper.SendMessageWrapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component("/exercise_info")
public class MuscleGroupCommand extends MainMenuCommand {

  @Override
  public boolean supports(Update update) {
    return update.getMessage().getText().equals("/exercise_info");
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    Long chatId = update.getMessage().getChatId();
    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
    List<InlineKeyboardButton> row = new ArrayList<>();

    var muscles = MuscleGroup.values();
    for (int i = 0; i < muscles.length; i++) {
      var button = new InlineKeyboardButton();
      String name = muscles[i].getName();
      button.setText(name);
      button.setCallbackData(name);
      row.add(button);
      if ((i + 1) % 2 == 0) {
        rows.add(row);
        row = new ArrayList<>();
      }
    }
    inlineKeyboardMarkup.setKeyboard(rows);

    return SendMessageWrapper.newBuilder()
        .chatId(chatId)
        .replyMarkup(inlineKeyboardMarkup)
        .text("Выберите группу мышц")
        .build();
  }

  @Override
  public String explanation() {
    return "to show muscle groups";
  }
}
