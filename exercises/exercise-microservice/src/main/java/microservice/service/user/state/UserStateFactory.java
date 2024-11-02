package microservice.service.user.state;

import org.springframework.stereotype.Component;

@Component
public class UserStateFactory {

  public UserState createUserSession(Long chatId) {
    UserState userState = new UserState();
    userState.setChatId(chatId);
    return userState;
  }
}
