package microservice.service;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityNotFoundException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import kvansipto.exercise.dto.UserDto;
import microservice.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserServiceTest {

  @Autowired
  UserService service;

  private final List<Long> idsToDelete = new ArrayList<>();

  @AfterEach
  void tearDown() {
    try {
      idsToDelete.forEach(id -> service.delete(id));
    } catch (EntityNotFoundException ignored) {
    }
  }

  @Test
  void testCreateUser() {
    Long userID = new Random(10).nextLong();

    UserDto expected = UserDto.builder()
        .id(userID)
        .userName("John Doe Test")
        .firstName("John")
        .lastName("Doe")
        .registeredAt(Timestamp.valueOf(LocalDateTime.now()))
        .build();

    service.create(expected);
    idsToDelete.add(userID);

    UserDto actual = service.getOne(userID);

    assertThat(expected)
        .usingRecursiveComparison()
        .withFailMessage(
            "Expecting UserDto %s to be the same as the userDto that was sent, but it was not", actual)
        .isEqualTo(actual);
  }

  @Test
  void toDto() {
    Long userID = new Random(10).nextLong();
    User expected = User.builder()
        .id(userID)
        .userName("John Doe Test")
        .firstName("John")
        .lastName("Doe")
        .registeredAt(Timestamp.valueOf(LocalDateTime.now()))
        .build();

    UserDto actual = service.toDto(expected);

    assertThat(expected)
        .usingRecursiveComparison()
        .ignoringFields("id")
        .withFailMessage(
            "Expecting UserDto %s to be the same as the user, but it was not", actual)
        .isEqualTo(actual);
  }

  @Test
  void toEntity() {
    Long userID = new Random(10).nextLong();
    UserDto expected = UserDto.builder()
        .id(userID)
        .userName("John Doe Test")
        .firstName("John")
        .lastName("Doe")
        .registeredAt(Timestamp.valueOf(LocalDateTime.now()))
        .build();

    User actual = service.toEntity(expected);

    assertThat(expected)
        .usingRecursiveComparison()
        .ignoringFields("id")
        .withFailMessage(
            "Expecting User %s to be the same as the userDto, but it was not", actual)
        .isEqualTo(actual);
  }

  @Test
  void getAll() {
    List<User> expected = new ArrayList<>();
    User user1 = User.builder()
        .id(new Random(10).nextLong())
        .userName("John Doe Test")
        .firstName("John")
        .lastName("Doe")
        .registeredAt(Timestamp.valueOf(LocalDateTime.now()))
        .build();

    User user2 = User.builder()
        .id(new Random(10).nextLong())
        .userName("Jane White Test")
        .firstName("Jane")
        .lastName("White")
        .registeredAt(Timestamp.valueOf(LocalDateTime.now()))
        .build();

    expected.add(user1);
    expected.add(user2);
    expected.forEach(user -> {
      service.create(user);
      idsToDelete.add(user.getId());
    });

    List<User> actual = (List<User>) service.getAll();

    assertThat(actual).containsAll(expected);
  }

  @Test
  void getAllAsDto() {
    UserDto userDto1 = UserDto.builder()
        .id(new Random(10).nextLong())
        .userName("John Doe Test")
        .firstName("John")
        .lastName("Doe")
        .registeredAt(Timestamp.valueOf(LocalDateTime.now()))
        .build();

    UserDto userDto2 = UserDto.builder()
        .id(new Random(10).nextLong())
        .userName("Jane White Test")
        .firstName("Jane")
        .lastName("White")
        .registeredAt(Timestamp.valueOf(LocalDateTime.now()))
        .build();

    List<UserDto> expected = new ArrayList<>();
    expected.add(userDto1);
    expected.add(userDto2);
    expected.forEach(user -> {
      service.create(user);
      idsToDelete.add(user.getId());
    });

    List<UserDto> actual = service.getAllAsDto();

    assertThat(actual).containsAll(expected);
  }

  @Test
  void update() {
    Long userID = new Random(10).nextLong();
    User user = User.builder()
        .id(userID)
        .userName("userName")
        .lastName("lastName")
        .firstName("firstName")
        .registeredAt(Timestamp.valueOf(LocalDateTime.now()))
        .build();

    service.create(user);
    idsToDelete.add(user.getId());

    User updatedUser = User.builder()
        .id(userID)
        .userName("updatedName")
        .firstName("updatedFirstName")
        .lastName("updatedLastName")
        .registeredAt(Timestamp.valueOf(LocalDateTime.now()))
        .build();

    service.update(updatedUser);

    UserDto actual = service.getOne(userID);

    assertThat(updatedUser)
        .usingRecursiveComparison()
        .ignoringFields("id")
        .withFailMessage("Expecting User %s to be the same as the updated user", actual)
        .isEqualTo(actual);
  }

  @Test
  void exists() {
    Long userID = new Random(10).nextLong();
    User user = User.builder()
        .id(userID)
        .userName("userName")
        .lastName("lastName")
        .firstName("firstName")
        .registeredAt(Timestamp.valueOf(LocalDateTime.now()))
        .build();
    assertThat(service.exists(userID)).isFalse();

    service.create(user);
    idsToDelete.add(user.getId());

    assertThat(service.exists(userID)).isTrue();
  }

  @Test
  void delete() {
    Long userID = new Random(10).nextLong();
    User user = User.builder()
        .id(userID)
        .userName("userName")
        .lastName("lastName")
        .firstName("firstName")
        .registeredAt(Timestamp.valueOf(LocalDateTime.now()))
        .build();
    service.create(user);
    idsToDelete.add(user.getId());

    assertThat(service.exists(userID)).isTrue();
    service.delete(userID);
    assertThat(service.exists(userID)).isFalse();
  }
}
