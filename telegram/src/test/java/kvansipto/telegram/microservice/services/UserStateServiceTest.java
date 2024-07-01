package kvansipto.telegram.microservice.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserStateServiceTest {

  private UserStateService userStateService;
  private UserState userState;
  private final String chatId = "123456";

  @BeforeEach
  void setUp() {
    userStateService = new UserStateService();
    userState = new UserState();
    userState.setChatId(chatId);
  }

  @Test
  void getCurrentState_shouldReturnNullWhenNoStateIsSet() {
    // Act
    UserState result = userStateService.getCurrentState(chatId);

    // Assert
    assertThat(result).isNull();
  }

  @Test
  void setCurrentState_shouldStoreStateCorrectly() {
    // Act
    userStateService.setCurrentState(chatId, userState);

    // Assert
    UserState result = userStateService.getCurrentState(chatId);
    assertThat(result).isNotNull();
    assertThat(result.getChatId()).isEqualTo(chatId);
  }

  @Test
  void getCurrentState_shouldReturnStoredState() {
    // Arrange
    userStateService.setCurrentState(chatId, userState);

    // Act
    UserState result = userStateService.getCurrentState(chatId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getChatId()).isEqualTo(chatId);
  }

  @Test
  void removeUserState_shouldRemoveStoredState() {
    // Arrange
    userStateService.setCurrentState(chatId, userState);

    // Act
    userStateService.removeUserState(chatId);

    // Assert
    UserState result = userStateService.getCurrentState(chatId);
    assertThat(result).isNull();
  }
}
