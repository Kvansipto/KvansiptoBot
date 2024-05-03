package io.project.KvansiptoBot.service;

import com.vdurmont.emoji.EmojiParser;
import io.project.KvansiptoBot.config.BotConfig;
import io.project.KvansiptoBot.model.Ads;
import io.project.KvansiptoBot.model.AdsRepository;
import io.project.KvansiptoBot.model.User;
import io.project.KvansiptoBot.model.UserRepository;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
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

  public static final String YES_BUTTON = "YES_BUTTON";
  public static final String NO_BUTTON = "NO_BUTTON";
  static final String HELP_TEXT = "This bot was made by Kvansipto\n\n"
      + "You can execute command from the main menu on the left or by typing a command:\n\n"
      + "Type /start to see welcome message\n\n"
      + "Type /mydata to see data stored about yourself\n\n"
      + "Type /help to see this message again";

  public TelegramBot(BotConfig config) {
    this.config = config;
    List<BotCommand> commandList = new ArrayList<>();
    commandList.add(new BotCommand("/start", "get welcome message"));
    commandList.add(new BotCommand("/getdata", "get your data store"));
    commandList.add(new BotCommand("/deletedata", "delete my data"));
    commandList.add(new BotCommand("/help", "how to use this bot"));
    commandList.add(new BotCommand("/settings", "set your preferences"));
    try {
      this.execute(new SetMyCommands(commandList, new BotCommandScopeDefault(), null));
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getBotUsername() {
    return config.getBotName();
  }

  @Override
  public void onUpdateReceived(Update update) {

    if (update.hasMessage() && update.getMessage().hasText()) {
      String message = update.getMessage().getText();
      long chatId = update.getMessage().getChatId();

      if (message.startsWith("/send") && config.getOwnerChatId().equals(chatId)) {
        var textToSend = EmojiParser.parseToUnicode(message.substring(message.indexOf(" ")));
        var users = userRepository.findAll();
        for (User user : users) {
          sendMessage(user.getChatId(), textToSend);
        }
      } else {
        switch (message) {
          case "/start":
            registerUser(update.getMessage());
            startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
            break;
          case "/help":
            sendMessage(chatId, HELP_TEXT);
            break;
          case "/register":
            register(chatId);
            break;
          default:
            sendMessage(chatId, "Sorry, command wasn't recognized");
        }
      }
    } else if (update.hasCallbackQuery()) {
      String callbackQuery = update.getCallbackQuery().getData();
      int messageId = update.getCallbackQuery().getMessage().getMessageId();
      long chatId = update.getCallbackQuery().getMessage().getChatId();
      if (callbackQuery.equals(YES_BUTTON)) {
        String text = "You pressed YES button";
        executeEditMessage(chatId, text, messageId);
      } else if (callbackQuery.equals(NO_BUTTON)) {
        String text = "You pressed NO button";
        executeEditMessage(chatId, text, messageId);
      }
    }
  }

  private void executeEditMessage(long chatId, String text, int messageId) {
    EditMessageText editMessageText = new EditMessageText();
    editMessageText.setChatId(chatId);
    editMessageText.setText(text);
    editMessageText.setMessageId(messageId);
    executeTelegramAction(editMessageText);
  }

  private void register(long chatId) {

    SendMessage message = new SendMessage();
    message.setChatId(chatId);
    message.setText("Do you really want to register?");

    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
    List<InlineKeyboardButton> row = new ArrayList<>();

    InlineKeyboardButton yesButton = new InlineKeyboardButton();
    yesButton.setText("Yes");
    yesButton.setCallbackData(YES_BUTTON);

    InlineKeyboardButton noButton = new InlineKeyboardButton();
    noButton.setText("No");
    noButton.setCallbackData(NO_BUTTON);

    row.add(yesButton);
    row.add(noButton);
    rows.add(row);
    inlineKeyboardMarkup.setKeyboard(rows);
    message.setReplyMarkup(inlineKeyboardMarkup);

    executeMessage(message);
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
    String answer = EmojiParser.parseToUnicode("Hi, " + name + "! Nice to meet you!" + " :fire:");
    sendMessage(chatId, answer, getDefaultReplyKeyboardMarkup());
  }

  private void sendMessage(long chatId, String messageToSend, ReplyKeyboardMarkup keyboardMarkup) {
    SendMessage message = new SendMessage();
    message.setChatId(chatId);
    message.setText(messageToSend);
    message.setReplyMarkup(keyboardMarkup);
    executeMessage(message);
  }

  private void sendMessage(long chatId, String messageToSend) {
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

  @Scheduled(cron = "${cron.scheduler}")
  private void sendAids(){
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
