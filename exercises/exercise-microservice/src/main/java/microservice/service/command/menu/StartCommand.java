package microservice.service.command.menu;

import com.vdurmont.emoji.EmojiParser;
import kvansipto.exercise.dto.UserDto;
import kvansipto.exercise.wrapper.SendMessageWrapper;
import microservice.service.UserService;
import microservice.service.event.UserInputCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("/start")
public class StartCommand extends MainMenuCommand {

  @Autowired
  private UserService userService;

  @Override
  public void process(UserInputCommandEvent event) {
    registerUser(event.update().getUser());
    String answer = EmojiParser.parseToUnicode(
        "Hi, " + event.update().getUser().getFirstName() + "! Nice to meet you!" + " :fire:");

    kafkaTemplate.send("actions-from-exercises", event.chatId(),
        SendMessageWrapper.newBuilder()
            .chatId(event.chatId())
            .text(answer)
            .build());
  }

  @Override
  public String explanation() {
    return "to register a user";
  }

  private void registerUser(UserDto user) {
    if (!userService.exists(user.getId())) {
      userService.create(user);
    }
  }
}
