package io.project.kvansiptobot.service;

import com.vdurmont.emoji.EmojiParser;
import io.project.kvansiptobot.config.BotConfig;
import io.project.kvansiptobot.model.Ads;
import io.project.kvansiptobot.model.AdsRepository;
import io.project.kvansiptobot.model.Exercise;
import io.project.kvansiptobot.model.ExerciseRepository;
import io.project.kvansiptobot.model.ExerciseResult;
import io.project.kvansiptobot.model.ExerciseResultRepository;
import io.project.kvansiptobot.model.MuscleCommand;
import io.project.kvansiptobot.model.MuscleGroup;
import io.project.kvansiptobot.model.User;
import io.project.kvansiptobot.model.UserRepository;
import jakarta.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramBot extends TelegramLongPollingBot {

  final BotConfig config;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private AdsRepository adsRepository;
  @Autowired
  private ExerciseRepository exerciseRepository;
  @Autowired
  private CommandFactory commandFactory;
  @Autowired
  private UserSessionFactory userSessionFactory;

  private Map<Long, UserSession> userStates = new HashMap<>();
  private final Map<String, Consumer<Update>> commands = new HashMap<>();
  private final Map<String, MuscleGroup> muscleGroupMap;
  private final Map<String, Exercise> exerciseMap = new HashMap<>();

  static final String START_COMMAND_TEXT = "/start";
  static final String EXERCISE_COMMAND_TEXT = "/exercises";
  static final String HELP_COMMAND_TEXT = "/help";

  static final String HELP_TEXT = "This bot was made by Kvansipto\n\n"
      + "You can execute command from the main menu on the left or by typing a command:\n\n"
      + "Type" + START_COMMAND_TEXT + " to see welcome message\n\n"
      + "Type" + EXERCISE_COMMAND_TEXT + " to see exercises and add results\n\n"
      + "Type" + HELP_COMMAND_TEXT + " to see this message again";
  @Autowired
  private ExerciseResultRepository exerciseResultRepository;

  public TelegramBot(BotConfig config) {
    this.config = config;
    List<BotCommand> commandList = new ArrayList<>();
    commandList.add(new BotCommand(START_COMMAND_TEXT, "get welcome message"));
    commandList.add(new BotCommand(EXERCISE_COMMAND_TEXT, "get exercises info"));
    commandList.add(new BotCommand(HELP_COMMAND_TEXT, "how to use this bot"));
    muscleGroupMap = Arrays.stream(MuscleGroup.values()).collect(Collectors.toMap(MuscleGroup::getName, m -> m));
    try {
      this.execute(new SetMyCommands(commandList, new BotCommandScopeDefault(), null));
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  @PostConstruct
  public void init() {
    exerciseRepository.findAll().forEach(exercise -> exerciseMap.put(exercise.getName(), exercise));
    commands.put(START_COMMAND_TEXT, this::handleStartCommand);
    commands.put(EXERCISE_COMMAND_TEXT, this::handleExerciseInfo);
    commands.put(HELP_COMMAND_TEXT, this::handleHelpCommand);
    muscleGroupMap.forEach((k, v) -> commands.put(k, this::handleMuscleCommand));
    exerciseMap.forEach((k, v) -> commands.put(k, this::handleExerciseCommand));
  }

  //TODO Ввод даты неудобный, попробовать сделать через кнопки
  private void promptForExerciseResult(long chatId, String exerciseName) {
    Exercise exercise = exerciseMap.get(exerciseName);
    setCurrentExercise(chatId, exercise);
    sendMessage(chatId, "Введите результат в формате:\n \\[День\\/Месяц вес\\(кг\\) количество "
        + "подходов "
        + "количество повторений\\]\n\n" + "Пример сообщения: 30\\/12 12\\.5 8 15");
  }

  @Override
  public String getBotUsername() {
    return config.getBotName();
  }

  @EventListener
  public void handleMuscleCommandEvent(MuscleCommandEvent muscleCommandEvent) {
    System.out.println("Called handleCommandEvent");
    SendMessage message = new SendMessage();
    message.setChatId(muscleCommandEvent.getChatId());
    message.setText(muscleCommandEvent.getMessage());
    message.setReplyMarkup(muscleCommandEvent.getReplyKeyboard());
    message.setParseMode("MarkdownV2");
    message.setDisableWebPagePreview(false);
    executeMessage(message);
  }

  @Override
  public void onUpdateReceived(Update update) {
    Consumer<Update> command;

    if (update.hasMessage() && update.getMessage().hasText()) {
      handleUserInput(update);
    } else if (update.hasCallbackQuery()) {
      command = commands.get(update.getCallbackQuery().getData());
      if (command != null) {
        command.accept(update);
      } else if (update.getCallbackQuery().getData().startsWith("ADD_EXERCISE_RESULT_")) {
        String exerciseName = update.getCallbackQuery().getData().split("_")[3];
        promptForExerciseResult(update.getCallbackQuery().getMessage().getChatId(), exerciseName);
      } else {
        long chatId = update.getMessage().getChatId();
        sendMessage(chatId, "Sorry, command wasn't recognized");
      }
    }
  }

  private void handleUserInput(Update update) {
    Consumer<Update> command = commands.get(update.getMessage().getText());
    Long chatId = update.getMessage().getChatId();
    String message = update.getMessage().getText();
    var userCurrentState = userStates.get(chatId);
    if (userCurrentState != null && userCurrentState.getIsWaitingForResult()) {
      processExerciseResult(chatId, message);
      userStates.remove(chatId);
    } else if (command != null) {
      command.accept(update);
    } else {
      sendMessage(chatId, "Sorry, command wasn't recognized");
    }
  }

  private void setCurrentExercise(Long chatId, Exercise exercise) {
    UserSession session = userSessionFactory.createUserSession(chatId);
    session.setChatId(chatId);
    session.setCurrentExercise(exercise);
    session.setIsWaitingForResult(true);
    userStates.put(chatId, session);
  }

  private void processExerciseResult(Long chatId, String message) {
    try {
      String[] parts = message.split(" ");
      String[] dateParts = parts[0].split("/");
      LocalDate date = LocalDate.of(LocalDate.now().getYear(), Integer.parseInt(dateParts[1]),
          Integer.parseInt(dateParts[0]));
      double weight = Double.parseDouble(parts[1]);
      byte sets = Byte.parseByte(parts[2]);
      byte reps = Byte.parseByte(parts[3]);
      ExerciseResult exerciseResult = new ExerciseResult();
      exerciseResult.setWeight(weight);
      exerciseResult.setNumberOfSets(sets);
      exerciseResult.setNumberOfRepetitions(reps);
      exerciseResult.setDate(date);
      exerciseResult.setUser(userRepository.findById(chatId).get());
      if (userStates.get(chatId).getCurrentExercise() == null) {
        System.out.println("Нет упражнения");
      } else {
        System.out.println("Текущее упражнение: " + userStates.get(chatId).getCurrentExercise().getName());
      }
      exerciseResult.setExercise(userStates.get(chatId).getCurrentExercise());
      exerciseResultRepository.save(exerciseResult);
      sendMessage(chatId, "Результат успешно сохранен");
    } catch (Exception e) {
      sendMessage(chatId, "Неверный формат ввода\\. Пожалуйста, введите данные снова\\.");
    }
  }

  private void handleMuscleCommand(Update update) {
    MuscleCommand muscleCommand = commandFactory
        .createMuscleCommand(muscleGroupMap.get(update.getCallbackQuery().getData()));
    muscleCommand.execute(update.getCallbackQuery().getMessage().getChatId());
  }

  private void handleExerciseCommand(Update update) {
    String callbackQuery = update.getCallbackQuery().getData();
    long chatId = update.getCallbackQuery().getMessage().getChatId();
    Exercise exercise = exerciseMap.get(callbackQuery);
    SendPhoto sendPhoto = new SendPhoto();
    sendPhoto.setChatId(chatId);
    sendPhoto.setPhoto(new InputFile(exercise.getImageUrl()));
    sendPhoto.setCaption(exercise.getDescription());
    executeTelegramAction(sendPhoto);

    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
    List<InlineKeyboardButton> row = new ArrayList<>();
    InlineKeyboardButton button = new InlineKeyboardButton();
    button.setCallbackData("ADD_EXERCISE_RESULT_" + exercise.getName());
    button.setText("Добавить результат выполнения упражнения");
    row.add(button);
    rows.add(row);
    inlineKeyboardMarkup.setKeyboard(rows);

    sendMessage(chatId, String.format("Посмотрите видео с упражнением на YouTube: [Смотреть видео](%s)",
        exercise.getVideoUrl()), inlineKeyboardMarkup);
  }

  private void handleStartCommand(Update update) {
    var message = update.getMessage();
    registerUser(message);
    startCommandReceived(message.getChatId(), update.getMessage().getChat().getFirstName());
  }

  private void handleHelpCommand(Update update) {
    sendMessage(update.getMessage().getChatId(), HELP_TEXT);
  }

  private void handleExerciseInfo(Update update) {
    generateMuscleGroupButtons(update.getMessage().getChatId());
  }

  private void generateMuscleGroupButtons(long chatId) {
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
    sendMessage(chatId, "Выберите группу мышц", inlineKeyboardMarkup);
  }

  private void executeEditMessage(long chatId, String text, int messageId) {
    EditMessageText editMessageText = new EditMessageText();
    editMessageText.setChatId(chatId);
    editMessageText.setText(text);
    editMessageText.setMessageId(messageId);
    executeTelegramAction(editMessageText);
  }

  private void registerUser(Message message) {
    if (!userRepository.existsById(message.getChatId())) {
      var chatId = message.getChatId();
      var chat = message.getChat();

      User user = new User();
      user.setChatId(chatId);
      user.setUserName(chat.getUserName());
      user.setFirstName(chat.getFirstName());
      user.setLastName(chat.getLastName());
      user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
      userRepository.save(user);
    }
  }

  private void startCommandReceived(long chatId, String name) {
    String answer = EmojiParser.parseToUnicode("Hi, " + name + "\\! Nice to meet you\\!" + " :fire:");
    sendMessage(chatId, answer, getDefaultReplyKeyboardMarkup());
  }

  public void sendMessage(long chatId, String messageToSend, ReplyKeyboard keyboardMarkup) {
    SendMessage message = new SendMessage();
    message.setChatId(chatId);
    message.setText(messageToSend);
    message.setReplyMarkup(keyboardMarkup);
    message.setParseMode("MarkdownV2");
    message.setDisableWebPagePreview(false);
    executeMessage(message);
  }

  public void sendMessage(long chatId, String messageToSend) {
    sendMessage(chatId, messageToSend, null);
  }

  private void executeMessage(SendMessage message) {
    executeTelegramAction(message);
  }

  private void executeTelegramAction(BotApiMethod<?> botApiMethod) {
    try {
      execute(botApiMethod);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  private void executeTelegramAction(SendPhoto botApiMethod) {
    try {
      execute(botApiMethod);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  private static ReplyKeyboardMarkup getDefaultReplyKeyboardMarkup() {
    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
    List<KeyboardRow> rows = new ArrayList<>();
    KeyboardRow row = new KeyboardRow();
    row.add("weather");
    row.add("something");
    rows.add(row);
    row = new KeyboardRow();
    row.add("secondRowButton");
    row.add("secondRowButton2");
    row.add("secondRowButton3");
    rows.add(row);
    replyKeyboardMarkup.setKeyboard(rows);
    return replyKeyboardMarkup;
  }

  //  @Scheduled(cron = "${cron.scheduler}")
  private void sendAids() {
    var ads = adsRepository.findAll();
    var users = userRepository.findAll();
    for (Ads ad : ads) {
      for (User user : users) {
        sendMessage(user.getChatId(), ad.getText());
      }
    }
  }

  @Override
  public String getBotToken() {
    return config.getBotToken();
  }
}
