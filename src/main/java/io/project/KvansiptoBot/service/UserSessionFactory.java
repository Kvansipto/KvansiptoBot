package io.project.KvansiptoBot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class UserSessionFactory {

  @Autowired
  private ApplicationContext applicationContext;

  public UserSession createUserSession(Long chatId) {
    UserSession userSession = applicationContext.getBean(UserSession.class);
    userSession.setChatId(chatId);
    return userSession;
  }
}
