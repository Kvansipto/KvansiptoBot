package kvansipto.telegram.microservice.services;

import org.springframework.stereotype.Component;

@Component
public class UserStateFactory {

  public UserState createUserSession(String chatId) {
    UserState userState = new UserState();
    userState.setChatId(chatId);
    return userState;
  }
}
