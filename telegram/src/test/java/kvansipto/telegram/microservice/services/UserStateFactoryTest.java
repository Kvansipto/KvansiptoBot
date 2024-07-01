package kvansipto.telegram.microservice.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserStateFactoryTest {

  UserStateFactory userStateFactory = new UserStateFactory();

  @Test
  void createUserSession_shouldReturnUserStateWithCorrectChatId() {
    // Arrange
    String chatId = "123456";

    // Act
    UserState userState = userStateFactory.createUserSession(chatId);

    // Assert
    assertThat(userState).isNotNull();
    assertThat(userState.getChatId()).isEqualTo(chatId);
  }
}
