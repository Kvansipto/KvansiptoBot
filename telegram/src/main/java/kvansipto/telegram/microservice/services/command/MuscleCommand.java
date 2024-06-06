package kvansipto.telegram.microservice.services.command;

import java.util.List;
import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.dto.AnswerData;
import kvansipto.telegram.microservice.services.dto.AnswerDto;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.EditMessageWrapper;
import kvansipto.telegram.microservice.utils.KeyboardMarkupUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class MuscleCommand extends Command {

  public static final String MUSCLE_COMMAND_TEXT = "Выберите упражнение";
  @Autowired
  RestToExercises restToExercises;

  @Override
  public boolean supports(Update update) {
    return update.hasCallbackQuery() &&
        AnswerData.deserialize(update.getCallbackQuery().getData()).getButtonCode().equals("muscle_group");
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
    String muscleGroup = AnswerData.deserialize(update.getCallbackQuery().getData()).getButtonText();
    System.out.println("Группа мышц " + muscleGroup);

    List<AnswerDto> answerDtoList = restToExercises.getExercisesByMuscleGroup(muscleGroup).stream()
        .map(e -> new AnswerDto(e.getName(), "exercise"))
        .toList();

    System.out.println("Упражнения для группы мышц " + muscleGroup + ": " + answerDtoList);

    return EditMessageWrapper.newBuilder()
        .chatId(chatId)
        .messageId(update.getCallbackQuery().getMessage().getMessageId())
        .replyMarkup(KeyboardMarkupUtil.createRows(answerDtoList, 1))
        .text(MUSCLE_COMMAND_TEXT)
        .build();
  }
}
