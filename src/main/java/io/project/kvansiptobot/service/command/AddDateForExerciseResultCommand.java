package io.project.kvansiptobot.service.command;

import io.project.kvansiptobot.model.Exercise;
import io.project.kvansiptobot.repository.ExerciseRepository;
import io.project.kvansiptobot.service.UserState;
import io.project.kvansiptobot.service.UserStateFactory;
import io.project.kvansiptobot.service.UserStateService;
import io.project.kvansiptobot.service.wrapper.BotApiMethodInterface;
import io.project.kvansiptobot.service.wrapper.SendMessageWrapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Component
public class AddDateForExerciseResultCommand extends Command {

  @Autowired
  ExerciseRepository exerciseRepository;
  @Autowired
  UserStateService userStateService;
  @Autowired
  private UserStateFactory userStateFactory;

  public static final String ADD_DATE_EXERCISE_RESULT_TEXT = "ADD_DATE_EXERCISE_RESULT_";

  @Override
  public boolean supports(Update update) {
    return update.hasCallbackQuery() && update.getCallbackQuery().getData()
        .startsWith(ExerciseCommand.ADD_EXERCISE_RESULT_TEXT);
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    String exerciseName = update.getCallbackQuery().getData().split("_")[3];
    Long chatId = update.getCallbackQuery().getMessage().getChatId();
    Exercise exercise = exerciseRepository.findByName(exerciseName);

    UserState userState = userStateFactory.createUserSession(chatId);
    userState.setCurrentExercise(exercise);
    userState.setCurrentState("CHOOSING DATE");
    userStateService.setCurrentState(chatId, userState);

    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
    List<InlineKeyboardButton> row = new ArrayList<>();
    InlineKeyboardButton today = new InlineKeyboardButton();
    InlineKeyboardButton yesterday = new InlineKeyboardButton();

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM");
    LocalDate localDate = LocalDate.now();

    today.setText("Сегодня");
    today.setCallbackData(ADD_DATE_EXERCISE_RESULT_TEXT + localDate.format(dtf));
    row.add(today);
    yesterday.setText("Вчера");
    yesterday.setCallbackData(ADD_DATE_EXERCISE_RESULT_TEXT + localDate.minusDays(1).format(dtf));
    row.add(yesterday);
    rows.add(row);
    row = new ArrayList<>();
    for (int i = 2; i <= 6; i++) {
      InlineKeyboardButton dateButton = new InlineKeyboardButton();
      String date = localDate.minusDays(i).format(dtf);
      dateButton.setText(date);
      dateButton.setCallbackData(ADD_DATE_EXERCISE_RESULT_TEXT + date);
      row.add(dateButton);
    }
    rows.add(row);
    inlineKeyboardMarkup.setKeyboard(rows);
    return SendMessageWrapper.newBuilder()
        .chatId(chatId)
        .replyMarkup(inlineKeyboardMarkup)
        .text("Выберите дату")
        .build();
  }
}
