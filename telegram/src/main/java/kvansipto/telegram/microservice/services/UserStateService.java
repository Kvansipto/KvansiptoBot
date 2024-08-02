package kvansipto.telegram.microservice.services;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class UserStateService {

  private final Map<Long, UserState> userStateHashMap = new HashMap<>();

  public UserState getCurrentState(Long chatId) {
    return userStateHashMap.get(chatId);
  }

  public void setCurrentState(Long chatId, UserState userState) {
    userStateHashMap.put(chatId, userState);
  }

  public void removeUserState(Long chatId) {
    userStateHashMap.remove(chatId);
  }
}
