package io.project.KvansiptoBot.service;

import com.vdurmont.emoji.EmojiParser;
import io.project.KvansiptoBot.config.BotConfig;
import io.project.KvansiptoBot.model.Ads;
import io.project.KvansiptoBot.model.AdsRepository;
import io.project.KvansiptoBot.model.Exercise;
import io.project.KvansiptoBot.model.ExerciseRepository;
import io.project.KvansiptoBot.model.MuscleCommand;
import io.project.KvansiptoBot.model.MuscleGroup;
import io.project.KvansiptoBot.model.User;
import io.project.KvansiptoBot.model.UserRepository;
import jakarta.annotation.PostConstruct;
import java.sql.Timestamp;
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

  private final Map<String, Consumer<Update>> commands = new HashMap<>();
  private final Map<String, MuscleGroup> muscleGroupMap;
  private final Map<String, Exercise> exerciseMap = new HashMap<>();

  static final String HELP_TEXT = "This bot was made by Kvansipto\n\n"
      + "You can execute command from the main menu on the left or by typing a command:\n\n"
      + "Type /start to see welcome message\n\n"
      + "Type /mydata to see data stored about yourself\n\n"
      + "Type /help to see this message again";

  public TelegramBot(BotConfig config) {
    this.config = config;
    List<BotCommand> commandList = new ArrayList<>();
    commandList.add(new BotCommand("/start", "get welcome message"));
    commandList.add(new BotCommand("/exerciseinfo", "get exercise info"));
    commandList.add(new BotCommand("/help", "how to use this bot"));
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
    commands.put("/start", this::handleStartCommand);
    commands.put("/exerciseinfo", this::handleExerciseInfo);
    commands.put("/help", this::handleHelpCommand);
    muscleGroupMap.forEach((k, v) -> commands.put(k, this::handleMuscleCommand));
    exerciseMap.forEach((k, v) -> commands.put(k, this::handleExerciseCommand));
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
    Consumer<Update> command = null;

    if (update.hasMessage() && update.getMessage().hasText()) {
      command = commands.get(update.getMessage().getText());
    } else if (update.hasCallbackQuery()) {
      command = commands.get(update.getCallbackQuery().getData());
    }
    if (command != null) {
      command.accept(update);
    } else {
      long chatId = update.getCallbackQuery().getMessage().getChatId();
      sendMessage(chatId, "Sorry, command wasn't recognized");
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
    sendMessage(chatId, String.format("Посмотрите видео с упражнением на YouTube: [Смотреть видео](%s)",
        exercise.getVideoUrl()));
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
    return config.getToken();
  }
}
