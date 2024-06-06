package kvansipto.telegram.microservice.services;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class UserStateService {

  private final Map<String, UserState> userStateHashMap = new HashMap<>();

  public UserState getCurrentState(String chatId) {
    return userStateHashMap.get(chatId);
  }

  public void setCurrentState(String chatId, UserState userState) {
    userStateHashMap.put(chatId, userState);
  }

  public void removeUserState(String chatId) {
    userStateHashMap.remove(chatId);
  }
}
