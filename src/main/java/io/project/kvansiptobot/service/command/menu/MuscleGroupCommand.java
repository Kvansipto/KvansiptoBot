package io.project.kvansiptobot.service.command.menu;

import io.project.kvansiptobot.model.MuscleGroup;
import io.project.kvansiptobot.service.wrapper.BotApiMethodInterface;
import io.project.kvansiptobot.service.wrapper.SendMessageWrapper;
import io.project.kvansiptobot.utils.KeyboardMarkupUtil;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component("/exercise_info")
public class MuscleGroupCommand extends MainMenuCommand {

  @Override
  public boolean supports(Update update) {
    return update.getMessage().getText().equals("/exercise_info");
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    Long chatId = update.getMessage().getChatId();
    var dataToInlineKeyboardMarkup = Arrays.stream(MuscleGroup.values())
        .map(MuscleGroup::getName)
        .toList();
    return SendMessageWrapper.newBuilder()
        .chatId(chatId)
        .replyMarkup(KeyboardMarkupUtil.generateInlineKeyboardMarkup(dataToInlineKeyboardMarkup, 2))
        .text("Выберите группу мышц")
        .build();
  }

  @Override
  public String explanation() {
    return "to show muscle groups";
  }
}
