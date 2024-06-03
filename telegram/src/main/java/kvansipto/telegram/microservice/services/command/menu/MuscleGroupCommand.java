package kvansipto.telegram.microservice.services.command.menu;

import java.util.List;
import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.dto.AnswerDto;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.SendMessageWrapper;
import kvansipto.telegram.microservice.utils.KeyboardMarkupUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component("/exercise_info")
public class MuscleGroupCommand extends MainMenuCommand {

  @Autowired
  RestToExercises restToExercises;

  @Override
  public boolean supports(Update update) {
    return update.hasMessage() && update.getMessage().getText().equals("/exercise_info");
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    Long chatId = update.getMessage().getChatId();

    List<AnswerDto> answerDtoList = restToExercises.getMuscleGroups().stream()
        .map(m -> new AnswerDto(m, "muscle_group"))
        .toList();

    return SendMessageWrapper.newBuilder()
        .chatId(chatId.toString())
        .replyMarkup(KeyboardMarkupUtil.createRows(answerDtoList, 2))
        .text("Выберите группу мышц")
        .build();
  }

  @Override
  public String explanation() {
    return "to show muscle groups";
  }
}
