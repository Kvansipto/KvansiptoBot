package microservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import kvansipto.exercise.dto.ExerciseDto;
import microservice.entity.Exercise;
import microservice.entity.MuscleGroup;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ExerciseServiceTest {

  @Autowired
  private ExerciseService service;

  private final List<Long> idsToDelete = new ArrayList<>();

  @AfterEach
  void tearDown() {
    try {
      idsToDelete.forEach(id -> service.delete(id));
    } catch (EntityNotFoundException ignored) {
    }
  }

  @Test
  void toDto() {
    Exercise exercise = Exercise.builder()
        .name("testExercise")
        .imageUrl("someImageUrl")
        .muscleGroup(MuscleGroup.CORE)
        .description("someDescription")
        .videoUrl("someVideoUrl")
        .build();

    ExerciseDto exerciseDto = service.toDto(exercise);

    assertAll(
        () -> assertThat(exercise)
            .usingRecursiveComparison()
            .ignoringFields("muscleGroup")
            .isEqualTo(exerciseDto),
        () -> assertThat(exerciseDto.getMuscleGroup().toLowerCase())
            .isEqualTo(exercise.getMuscleGroup().name().toLowerCase())
    );
  }

  @Test
  void toEntity() {
    ExerciseDto exerciseDto = ExerciseDto.builder()
        .id(new Random(10).nextLong())
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.CORE.name().toLowerCase())
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();

    Exercise exercise = service.toEntity(exerciseDto);

    assertAll(
        () -> assertThat(exerciseDto)
            .usingRecursiveComparison()
            .ignoringFields("muscleGroup", "id")
            .isEqualTo(exercise),
        () -> assertThat(exerciseDto.getMuscleGroup().toLowerCase())
            .isEqualTo(exercise.getMuscleGroup().name().toLowerCase())
    );
  }

  @Test
  void create() {
    Long exerciseId = new Random(10).nextLong();

    ExerciseDto expected = ExerciseDto.builder()
        .id(exerciseId)
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.CORE.name().toLowerCase())
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();

    service.create(expected);
    idsToDelete.add(exerciseId);

    ExerciseDto actual = service.getOne(exerciseId);
    assertThat(expected)
        .usingRecursiveComparison()
        .withFailMessage(
            "Expecting ExerciseDto %s to be the same as the exerciseDto that was sent, but it was not", actual)
        .isEqualTo(actual);
  }

  @Test
  void getAll() {
    List<Exercise> expected = new ArrayList<>();
    Exercise exercise1 = Exercise.builder()
        .id(new Random(10).nextLong())
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.BACK)
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    Exercise exercise2 = Exercise.builder()
        .id(new Random(10).nextLong())
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.CHEST)
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    expected.add(exercise1);
    expected.add(exercise2);
    expected.forEach(exercise -> {
      service.create(exercise);
      idsToDelete.add(exercise.getId());
    });
    List<Exercise> actual = (List<Exercise>) service.getAll();
    assertThat(actual).containsAll(expected);
  }

  @Test
  void getAllAsDto() {
    List<ExerciseDto> expected = new ArrayList<>();
    ExerciseDto exerciseDto1 = ExerciseDto.builder()
        .id(new Random(10).nextLong())
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.CORE.name().toLowerCase())
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    ExerciseDto exerciseDto2 = ExerciseDto.builder()
        .id(new Random(10).nextLong())
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.CORE.name().toLowerCase())
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    expected.add(exerciseDto1);
    expected.add(exerciseDto2);
    expected.forEach(exercise -> {
      service.create(exercise);
      idsToDelete.add(exercise.getId());
    });

    List<ExerciseDto> actual = service.getAllAsDto();
    assertThat(actual).containsAll(expected);
  }

  @Test
  void update() {
    Long exerciseId = new Random(10).nextLong();
    Exercise exercise = Exercise.builder()
        .id(exerciseId)
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.BACK)
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    service.create(exercise);
    idsToDelete.add(exerciseId);

    Exercise updatedExercise = Exercise.builder()
        .id(exerciseId)
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.BACK)
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    service.update(updatedExercise);
    ExerciseDto actual = service.getOne(exerciseId);
    assertAll(
        () -> assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields("muscleGroup")
            .isEqualTo(updatedExercise),
        () -> assertThat(actual.getMuscleGroup().toLowerCase())
            .isEqualTo(updatedExercise.getMuscleGroup().name().toLowerCase())
    );
  }

  @Test
  void exists() {
    Long exerciseId = new Random(10).nextLong();
    Exercise exercise = Exercise.builder()
        .id(exerciseId)
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.BACK)
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    assertThat(service.exists(exerciseId)).isFalse();
    service.create(exercise);
    idsToDelete.add(exerciseId);
    assertThat(service.exists(exerciseId)).isTrue();
  }

  @Test
  void delete() {
    Long exerciseId = new Random(10).nextLong();
    Exercise exercise = Exercise.builder()
        .id(exerciseId)
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.BACK)
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    service.create(exercise);
    idsToDelete.add(exerciseId);
    assertThat(service.exists(exerciseId)).isTrue();
    service.delete(exerciseId);
    assertThat(service.exists(exerciseId)).isFalse();
  }

  @Test
  void getExercisesByMuscleGroup() {
    List<ExerciseDto> expected = new ArrayList<>();
    ExerciseDto exerciseDto1 = ExerciseDto.builder()
        .id(new Random(10).nextLong())
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.CORE.name().toLowerCase())
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    ExerciseDto exerciseDto2 = ExerciseDto.builder()
        .id(new Random(10).nextLong())
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.CORE.name().toLowerCase())
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    expected.add(exerciseDto1);
    expected.add(exerciseDto2);
    expected.forEach(exercise -> {
      service.create(exercise);
      idsToDelete.add(exercise.getId());
    });
    List<ExerciseDto> actual = service.getExercisesByMuscleGroup(MuscleGroup.CORE.getName());
    assertThat(actual).containsAll(expected);
  }

  @Test
  void getExerciseByName() {
    String exerciseName = new RandomString(8).nextString();
    ExerciseDto expected = ExerciseDto.builder()
        .id(new Random(10).nextLong())
        .name(exerciseName)
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.CORE.name().toLowerCase())
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    service.create(expected);
    idsToDelete.add(expected.getId());
    ExerciseDto actual = service.getExerciseByName(exerciseName);
    assertThat(expected)
        .usingRecursiveComparison()
        .isEqualTo(actual);
  }
}