package microservice.service.command.menu;

import com.vdurmont.emoji.EmojiParser;
import kvansipto.exercise.dto.UserDto;
import kvansipto.exercise.wrapper.SendMessageWrapper;
import lombok.extern.slf4j.Slf4j;
import microservice.service.UserService;
import microservice.service.event.UserInputCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@CommandName("/start")
@Slf4j
public class StartCommand extends MainMenuCommand {

  @Autowired
  private UserService userService;

  @Override
  public void process(UserInputCommandEvent event) {
    log.info("Star processing command {}", this.getClass().getSimpleName());
    registerUser(event.update().getUser());
    String answer = EmojiParser.parseToUnicode(
        "Hi, " + event.update().getUser().getFirstName() + "! Nice to meet you!" + " :fire:");
    kafkaExerciseService.sendBotApiMethod(event.chatId(), SendMessageWrapper.newBuilder()
            .chatId(event.chatId())
            .text(answer)
            .build())
        .subscribe();
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
