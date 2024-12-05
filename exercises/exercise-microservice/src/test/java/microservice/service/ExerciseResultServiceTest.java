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
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.dto.UserDto;
import kvansipto.exercise.filter.ExerciseResultFilter;
import microservice.entity.Exercise;
import microservice.entity.ExerciseResult;
import microservice.entity.MuscleGroup;
import microservice.entity.User;
import microservice.service.postgre.AbstractPostgreTestContainerTestBase;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ExerciseResultServiceTest extends AbstractPostgreTestContainerTestBase {

  private static ExerciseResultService exerciseResultService;

  private static UserService userService;
  private static ExerciseService exerciseService;

  private static User user;
  private static Exercise exercise;

  private final List<Long> idsToDelete = new ArrayList<>();

  @BeforeAll
  static void setUpBeforeClass(@Autowired UserService userService, @Autowired ExerciseService exerciseService,
      @Autowired ExerciseResultService exerciseResultService) {
    ExerciseResultServiceTest.userService = userService;
    ExerciseResultServiceTest.exerciseService = exerciseService;
    ExerciseResultServiceTest.exerciseResultService = exerciseResultService;
    user = User.builder()
        .id(Math.abs(new Random().nextLong()))
        .userName("John Doe Test")
        .firstName("John")
        .lastName("Doe")
        .registeredAt(Timestamp.valueOf(LocalDateTime.now()))
        .build();
    user = userService.create(user);
    exercise = Exercise.builder()
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.BACK)
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    exercise = exerciseService.create(exercise);
    assertThat(userService.exists(user.getId())).isTrue();
    assertThat(exerciseService.exists(exercise.getId())).isTrue();
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
        .id(Math.abs(new Random().nextLong()))
        .exercise(exercise)
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now())
        .user(user)
        .build();

    ExerciseResultDto actual = exerciseResultService.toDto(expected);
    assertAll(
        () -> assertThat(actual.getNumberOfSets())
            .isEqualTo((int) expected.getNumberOfSets()),
        () -> assertThat(actual.getNumberOfRepetitions())
            .isEqualTo((int) expected.getNumberOfRepetitions()),
        () -> assertThat(actual.getExercise().getMuscleGroup().toLowerCase())
            .isEqualTo(expected.getExercise().getMuscleGroup().name().toLowerCase()),
        () -> assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields("numberOfSets", "numberOfRepetitions", "exercise.muscleGroup")
            .isEqualTo(expected)
    );
  }

  @Test
  void toEntity() {
    ExerciseResultDto expected = ExerciseResultDto.builder()
        .id(Math.abs(new Random().nextLong()))
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
            .ignoringFields("numberOfSets", "numberOfRepetitions", "exercise.muscleGroup")
            .isEqualTo(expected),
        () -> assertThat(expected.getExercise().getMuscleGroup().toLowerCase())
            .isEqualTo(actual.getExercise().getMuscleGroup().name().toLowerCase()),
        () -> assertThat((int) actual.getNumberOfSets())
            .isEqualTo(expected.getNumberOfSets()),
        () -> assertThat((int) actual.getNumberOfRepetitions())
            .isEqualTo(expected.getNumberOfRepetitions())
    );
  }

  @Test
  void create() {
    ExerciseResultDto expected = ExerciseResultDto.builder()
        .exercise(exerciseService.getOne(exercise.getId()))
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now())
        .user(userService.getOne(user.getId()))
        .build();
    var exerciseResultId = exerciseResultService.create(expected).getId();
    idsToDelete.add(exerciseResultId);
    ExerciseResultDto actual = exerciseResultService.getOne(exerciseResultId);
    assertThat(expected)
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(actual);
  }

  @Test
  void getAll() {
    List<ExerciseResult> expected = new ArrayList<>();
    ExerciseResult exerciseResult1 = ExerciseResult.builder()
        .exercise(exercise)
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now().minusDays(new Random().nextInt(10)))
        .user(user)
        .build();
    ExerciseResult exerciseResult2 = ExerciseResult.builder()
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
      ExerciseResult savedResult = exerciseResultService.create(exerciseResult);
      idsToDelete.add(savedResult.getId());
    });
    List<ExerciseResult> actual = (List<ExerciseResult>) exerciseResultService.getAll();
    assertThat(actual)
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
        .containsAll(expected);
  }

  @Test
  void getAllAsDto() {
    List<ExerciseResultDto> expected = new ArrayList<>();
    ExerciseDto exerciseDto = exerciseService.getOne(exercise.getId());
    UserDto userDto = userService.getOne(user.getId());
    ExerciseResultDto exerciseResultDto1 = ExerciseResultDto.builder()
        .exercise(exerciseDto)
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now())
        .user(userDto)
        .build();
    ExerciseResultDto exerciseResultDto2 = ExerciseResultDto.builder()
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
      ExerciseResultDto savedResult = exerciseResultService.create(exerciseResultDto);
      idsToDelete.add(savedResult.getId());
    });
    List<ExerciseResultDto> actual = exerciseResultService.getAllAsDto();
    assertThat(actual)
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
        .containsAll(expected);
  }

  @Test
  void update() {
    ExerciseResult exerciseResult = ExerciseResult.builder()
        .exercise(exercise)
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now().minusDays(new Random().nextInt(10)))
        .user(user)
        .build();
    ExerciseResult savedExerciseResult = exerciseResultService.create(exerciseResult);
    Long exerciseResultId = savedExerciseResult.getId();
    idsToDelete.add(exerciseResultId);

    User newUser = User.builder()
        .id(Math.abs(new Random().nextLong())) // Задаем ID только для User
        .userName(new RandomString(8).nextString())
        .firstName(new RandomString(8).nextString())
        .lastName(new RandomString(8).nextString())
        .registeredAt(Timestamp.valueOf(LocalDateTime.now().minusDays(5)))
        .build();
    userService.create(newUser);

    Exercise newExercise = Exercise.builder()
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.LEGS)
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    Exercise savedNewExercise = exerciseService.create(newExercise);

    ExerciseResult updatedExerciseResult = ExerciseResult.builder()
        .id(exerciseResultId)
        .exercise(savedNewExercise)
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
            .ignoringFields("id", "exercise.muscleGroup")
            .isEqualTo(exerciseResultService.toDto(updatedExerciseResult)),
        () -> assertThat(actual.getExercise().getMuscleGroup().toLowerCase())
            .isEqualTo(updatedExerciseResult.getExercise().getMuscleGroup().name().toLowerCase())
    );
  }

  @Test
  void exists() {
    ExerciseResult exerciseResult = ExerciseResult.builder()
        .exercise(exercise)
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now().minusDays(new Random().nextInt(10)))
        .user(user)
        .build();
    var exerciseResultId = exerciseResultService.create(exerciseResult).getId();
    assertThat(exerciseResultService.exists(exerciseResultId)).isTrue();
    idsToDelete.add(exerciseResult.getId());
  }

  @Test
  void delete() {
    ExerciseResult exerciseResult = ExerciseResult.builder()
        .exercise(exercise)
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now().minusDays(new Random().nextInt(10)))
        .user(user)
        .build();
    var exerciseResultId = exerciseResultService.create(exerciseResult).getId();
    idsToDelete.add(exerciseResultId);
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
        .exercise(exerciseDto)
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now().minusDays(new Random().nextInt(10)))
        .user(userDto)
        .build();
    ExerciseResultDto exerciseResult2 = ExerciseResultDto.builder()
        .exercise(exerciseDto)
        .weight(new Random().nextInt(100))
        .numberOfSets((byte) new Random().nextInt(100))
        .numberOfRepetitions((byte) new Random().nextInt(100))
        .date(LocalDate.now().minusDays(new Random().nextInt(10)))
        .user(userDto)
        .build();
    expected.add(exerciseResult1);
    expected.add(exerciseResult2);
    expected.forEach(exerciseResultDto -> {
      ExerciseResultDto savedResult = exerciseResultService.create(exerciseResultDto);
      idsToDelete.add(savedResult.getId());
    });
    ExerciseResultFilter filter = ExerciseResultFilter.builder()
        .exerciseDto(exerciseDto)
        .userChatId(userDto.getId())
        .build();
    List<ExerciseResultDto> actual = exerciseResultService.findExerciseResults(filter);
    assertThat(actual)
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
        .containsAll(expected);
  }
}