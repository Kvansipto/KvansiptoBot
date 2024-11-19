package kvansipto.telegram.microservice.services.command;

import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.dto.AnswerData;
import kvansipto.telegram.microservice.services.dto.AnswerDto;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.EditMessageWrapper;
import kvansipto.telegram.microservice.utils.KeyboardMarkupUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class MuscleCommand extends Command {

  private static final String MUSCLE_COMMAND_TEXT = "Выберите упражнение";
  private final RestToExercises restToExercises;

  @Override
  public boolean supports(Update update) {
    return update.hasCallbackQuery() &&
        AnswerData.deserialize(update.getCallbackQuery().getData()).getButtonCode().equals("muscle_group");
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    Long chatId = update.getCallbackQuery().getMessage().getChatId();
    String muscleGroup = AnswerData.deserialize(update.getCallbackQuery().getData()).getButtonText();
    log.debug("Группа мышц " + muscleGroup);

    var answerDtoList = restToExercises.getExercisesByMuscleGroup(muscleGroup).stream()
        .map(e -> new AnswerDto(e.getName(), "exercise"))
        .toList();

    log.debug("Упражнения для группы мышц " + muscleGroup + ": " + answerDtoList);

    return EditMessageWrapper.newBuilder()
        .chatId(chatId)
        .messageId(update.getCallbackQuery().getMessage().getMessageId())
        .replyMarkup(KeyboardMarkupUtil.createRows(answerDtoList, answerDtoList.size()))
        .text(MUSCLE_COMMAND_TEXT)
        .build();
  }
}
