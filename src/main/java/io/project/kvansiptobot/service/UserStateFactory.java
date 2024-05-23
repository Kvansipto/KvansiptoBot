package io.project.kvansiptobot.service;

import org.springframework.stereotype.Component;

@Component
public class UserStateFactory {

  public UserState createUserSession(long chatId) {
    UserState userState = new UserState();
    userState.setChatId(chatId);
    return userState;
  }
}
