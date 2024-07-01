package kvansipto.telegram.microservice.services.command.menu;

import com.vdurmont.emoji.EmojiParser;
import java.sql.Timestamp;
import kvansipto.exercise.dto.UserDto;
import kvansipto.telegram.microservice.services.RestToExercises;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.services.wrapper.SendMessageWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component("/start")
public class StartCommand extends MainMenuCommand {

  @Autowired
  private RestToExercises restToExercises;

  public static final String START_COMMAND_TEXT = "/start";

  @Override
  public boolean supports(Update update) {
    return update.hasMessage() && update.getMessage().getText().equals(START_COMMAND_TEXT);
  }

  @Override
  public BotApiMethodInterface process(Update update) {
    var message = update.getMessage();
    registerUser(message);
    String firstName = update.getMessage().getChat().getFirstName();
    String answer = EmojiParser.parseToUnicode("Hi, " + firstName + "! Nice to meet you!" + " :fire:");
    return SendMessageWrapper.newBuilder()
        .chatId(message.getChatId().toString())
        .text(answer)
        .build();
  }

  @Override
  public String explanation() {
    return "to register a user";
  }

  private void registerUser(Message message) {
    if (!restToExercises.userExists(message.getChatId().toString())) {
      var chatId = message.getChatId().toString();
      var chat = message.getChat();

      UserDto user = UserDto.builder()
          .id(chatId)
          .userName(chat.getUserName())
          .firstName(chat.getFirstName())
          .lastName(chat.getLastName())
          .registeredAt(new Timestamp(System.currentTimeMillis()))
          .build();
      restToExercises.saveUser(user);
    }
  }
}
