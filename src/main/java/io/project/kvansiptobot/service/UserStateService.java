package io.project.kvansiptobot.service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class UserStateService {

  private final Map<Long, UserState> userStateHashMap = new HashMap<>();

  public UserState getCurrentState(long chatId) {
    return userStateHashMap.get(chatId);
  }

  public void setCurrentState(long chatId, UserState userState) {
    userStateHashMap.put(chatId, userState);
  }

  public void removeUserState(long chatId) {
    userStateHashMap.remove(chatId);
  }
}
