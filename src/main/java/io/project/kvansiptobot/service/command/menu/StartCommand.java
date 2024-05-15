package io.project.kvansiptobot.service.command.menu;

import com.vdurmont.emoji.EmojiParser;
import io.project.kvansiptobot.model.User;
import io.project.kvansiptobot.repository.UserRepository;
import io.project.kvansiptobot.service.wrapper.BotApiMethodInterface;
import io.project.kvansiptobot.service.wrapper.SendMessageWrapper;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

@Component("/start")
public class StartCommand extends MainMenuCommand {

  @Autowired
  UserRepository userRepository;

  public static final String START_COMMAND_TEXT = "/start";

  @Override
  public boolean supports(Update update) {
    return update.getMessage().getText().equals(START_COMMAND_TEXT);
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    var message = update.getMessage();
    registerUser(message);
    String firstName = update.getMessage().getChat().getFirstName();
    String answer = EmojiParser.parseToUnicode("Hi, " + firstName + "! Nice to meet you!" + " :fire:");
    return SendMessageWrapper.newBuilder()
        .chatId(message.getChatId())
        .text(answer)
        .replyMarkup(getDefaultReplyKeyboardMarkup())
        .build();
  }

  @Override
  public String explanation() {
    return "to register a user";
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

  private ReplyKeyboardMarkup getDefaultReplyKeyboardMarkup() {
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
}
