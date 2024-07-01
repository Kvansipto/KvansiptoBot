package microservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.persistence.EntityNotFoundException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.dto.UserDto;
import kvansipto.exercise.filter.ExerciseResultFilter;
import microservice.entity.Exercise;
import microservice.entity.ExerciseResult;
import microservice.entity.MuscleGroup;
import microservice.entity.User;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ExerciseResultServiceTest {

  @Autowired
  private ExerciseResultService exerciseResultService;

  private static UserService userService;
  private static ExerciseService exerciseService;

  private static User user;
  private static Exercise exercise;

  private final List<String> idsToDelete = new ArrayList<>();

  @BeforeAll
  static void setUpBeforeClass(@Autowired UserService userService, @Autowired ExerciseService exerciseService) {
    ExerciseResultServiceTest.userService = userService;
    ExerciseResultServiceTest.exerciseService = exerciseService;
    user = User.builder()
        .id(UUID.randomUUID().toString())
        .userName("John Doe Test")
        .firstName("John")
        .lastName("Doe")
        .registeredAt(Timestamp.valueOf(LocalDateTime.now()))
        .build();
    userService.create(user);
    exercise = Exercise.builder()
        .id(UUID.randomUUID().toString())
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.BACK)
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    exerciseService.create(exercise);
  }

  @AfterEach
  void tearDown() {
    try {
      idsToDelete.forEach(id -> exerciseResultService.delete(id));
    } catch (EntityNotFoundException ignored) {
    }
  }

  @AfterAll
  static void tearDownAfterClass() {
    try {
      exerciseService.delete(exercise.getId());
      userService.delete(user.getId());
    } catch (EntityNotFoundException ignored) {
    }
  }

  @Test
  void toDto() {
    ExerciseResult expected = ExerciseResult.builder()
        .id(UUID.randomUUID().toString())
        .exercise(exercise)
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now())
        .user(user)
        .build();

    ExerciseResultDto actual = exerciseResultService.toDto(expected);
    assertAll(
        () -> assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields("exercise.muscleGroup")
            .isEqualTo(expected),
        () -> assertThat(actual.getExercise().getMuscleGroup().toLowerCase())
            .isEqualTo(expected.getExercise().getMuscleGroup().name().toLowerCase())
    );
  }

  @Test
  void toEntity() {
    ExerciseResultDto expected = ExerciseResultDto.builder()
        .id(UUID.randomUUID().toString())
        .exercise(exerciseService.getOne(exercise.getId()))
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now())
        .user(userService.getOne(user.getId()))
        .build();
    ExerciseResult actual = exerciseResultService.toEntity(expected);

    assertAll(
        () -> assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields("exercise.muscleGroup")
            .isEqualTo(expected),
        () -> assertThat(expected.getExercise().getMuscleGroup().toLowerCase())
            .isEqualTo(actual.getExercise().getMuscleGroup().name().toLowerCase())
    );
  }

  @Test
  void create() {
    String exerciseResultId = UUID.randomUUID().toString();
    ExerciseResultDto expected = ExerciseResultDto.builder()
        .id(exerciseResultId)
        .exercise(exerciseService.getOne(exercise.getId()))
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now())
        .user(userService.getOne(user.getId()))
        .build();
    exerciseResultService.create(expected);
    idsToDelete.add(expected.getId());
    ExerciseResultDto actual = exerciseResultService.getOne(exerciseResultId);
    assertThat(expected)
        .usingRecursiveComparison()
        .isEqualTo(actual);
  }

  @Test
  void getAll() {
    List<ExerciseResult> expected = new ArrayList<>();
    ExerciseResult exerciseResult1 = ExerciseResult.builder()
        .id(UUID.randomUUID().toString())
        .exercise(exercise)
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now().minusDays(new Random().nextInt(10)))
        .user(user)
        .build();
    ExerciseResult exerciseResult2 = ExerciseResult.builder()
        .id(UUID.randomUUID().toString())
        .exercise(exercise)
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now().minusDays(new Random().nextInt(10)))
        .user(user)
        .build();
    expected.add(exerciseResult1);
    expected.add(exerciseResult2);
    expected.forEach(exerciseResult -> {
      exerciseResultService.create(exerciseResult);
      idsToDelete.add(exerciseResult.getId());
    });
    List<ExerciseResult> actual = (List<ExerciseResult>) exerciseResultService.getAll();
    assertThat(actual).containsAll(expected);
  }

  @Test
  void getAllAsDto() {
    List<ExerciseResultDto> expected = new ArrayList<>();
    ExerciseDto exerciseDto = exerciseService.getOne(exercise.getId());
    UserDto userDto = userService.getOne(user.getId());
    ExerciseResultDto exerciseResultDto1 = ExerciseResultDto.builder()
        .id(UUID.randomUUID().toString())
        .exercise(exerciseDto)
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now())
        .user(userDto)
        .build();
    ExerciseResultDto exerciseResultDto2 = ExerciseResultDto.builder()
        .id(UUID.randomUUID().toString())
        .exercise(exerciseDto)
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now())
        .user(userDto)
        .build();
    expected.add(exerciseResultDto1);
    expected.add(exerciseResultDto2);
    expected.forEach(exerciseResultDto -> {
      exerciseResultService.create(exerciseResultDto);
      idsToDelete.add(exerciseResultDto.getId());
    });
    List<ExerciseResultDto> actual = exerciseResultService.getAllAsDto();
    assertThat(actual).containsAll(expected);
  }

  @Test
  void update() {
    String exerciseResultId = UUID.randomUUID().toString();
    ExerciseResult exerciseResult = ExerciseResult.builder()
        .id(exerciseResultId)
        .exercise(exercise)
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now().minusDays(new Random().nextInt(10)))
        .user(user)
        .build();
    exerciseResultService.create(exerciseResult);
    idsToDelete.add(exerciseResult.getId());

    User newUser = User.builder()
        .id(UUID.randomUUID().toString())
        .userName(new RandomString(8).nextString())
        .firstName(new RandomString(8).nextString())
        .lastName(new RandomString(8).nextString())
        .registeredAt(Timestamp.valueOf(LocalDateTime.now().minusDays(5)))
        .build();
    userService.create(newUser);
    Exercise newExercise = Exercise.builder()
        .id(UUID.randomUUID().toString())
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.LEGS)
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    exerciseService.create(newExercise);
    ExerciseResult updatedExerciseResult = ExerciseResult.builder()
        .id(exerciseResultId)
        .exercise(newExercise)
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now().minusDays(new Random().nextInt(10)))
        .user(newUser)
        .build();
    exerciseResultService.update(updatedExerciseResult);
    ExerciseResultDto actual = exerciseResultService.getOne(exerciseResultId);
    assertAll(
        () -> assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields("exercise.muscleGroup")
            .isEqualTo(updatedExerciseResult),
        () -> assertThat(actual.getExercise().getMuscleGroup().toLowerCase())
            .isEqualTo(updatedExerciseResult.getExercise().getMuscleGroup().name().toLowerCase())
    );
  }

  @Test
  void exists() {
    String exerciseResultId = UUID.randomUUID().toString();
    ExerciseResult exerciseResult = ExerciseResult.builder()
        .id(exerciseResultId)
        .exercise(exercise)
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now().minusDays(new Random().nextInt(10)))
        .user(user)
        .build();
    assertThat(exerciseResultService.exists(exerciseResultId)).isFalse();
    exerciseResultService.create(exerciseResult);
    idsToDelete.add(exerciseResult.getId());
    assertThat(exerciseResultService.exists(exerciseResultId)).isTrue();
  }

  @Test
  void delete() {
    String exerciseResultId = UUID.randomUUID().toString();
    ExerciseResult exerciseResult = ExerciseResult.builder()
        .id(exerciseResultId)
        .exercise(exercise)
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now().minusDays(new Random().nextInt(10)))
        .user(user)
        .build();
    exerciseResultService.create(exerciseResult);
    idsToDelete.add(exerciseResult.getId());
    assertThat(exerciseResultService.exists(exerciseResultId)).isTrue();
    exerciseResultService.delete(exerciseResultId);
    assertThat(exerciseResultService.exists(exerciseResultId)).isFalse();
  }

  @Test
  void getExerciseResults() {
    List<ExerciseResultDto> expected = new ArrayList<>();
    ExerciseDto exerciseDto = exerciseService.getOne(exercise.getId());
    UserDto userDto = userService.getOne(user.getId());
    ExerciseResultDto exerciseResult1 = ExerciseResultDto.builder()
        .id(UUID.randomUUID().toString())
        .exercise(exerciseDto)
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now().minusDays(new Random().nextInt(10)))
        .user(userDto)
        .build();
    ExerciseResultDto exerciseResult2 = ExerciseResultDto.builder()
        .id(UUID.randomUUID().toString())
        .exercise(exerciseDto)
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now().minusDays(new Random().nextInt(10)))
        .user(userDto)
        .build();
    expected.add(exerciseResult1);
    expected.add(exerciseResult2);
    expected.forEach(exerciseResult -> {
      exerciseResultService.create(exerciseResult);
      idsToDelete.add(exerciseResult.getId());
    });
    Page<ExerciseResultDto> expectedPage = new PageImpl<>(expected);
    ExerciseResultFilter filter = ExerciseResultFilter.builder()
        .exerciseDto(exerciseDto)
        .userChatId(userDto.getId())
        .build();
    Page<ExerciseResultDto> actual = exerciseResultService.getExerciseResults(filter, PageRequest.of(0, 10));
    assertThat(actual).containsAll(expectedPage);
  }
}