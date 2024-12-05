package microservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import kvansipto.exercise.dto.ExerciseDto;
import microservice.entity.Exercise;
import microservice.entity.MuscleGroup;
import microservice.service.postgre.AbstractPostgreTestContainerTestBase;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ExerciseServiceTest extends AbstractPostgreTestContainerTestBase {

  @Autowired
  private ExerciseService service;

  private final List<Long> idsToDelete = new ArrayList<>();

  @AfterEach
  void tearDown() {
    idsToDelete.forEach(id -> {
      try {
        service.delete(id);
      } catch (EntityNotFoundException ignored) {
      }
    });
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
        () -> assertThat(exerciseDto)
            .usingRecursiveComparison()
            .ignoringFields("muscleGroup")
            .isEqualTo(exercise),
        () -> assertThat(exerciseDto.getMuscleGroup().toLowerCase())
            .isEqualTo(exercise.getMuscleGroup().name().toLowerCase())
    );
  }

  @Test
  void toEntity() {
    ExerciseDto exerciseDto = ExerciseDto.builder()
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
            .ignoringFields("muscleGroup")
            .isEqualTo(exercise),
        () -> assertThat(exerciseDto.getMuscleGroup().toLowerCase())
            .isEqualTo(exercise.getMuscleGroup().name().toLowerCase())
    );
  }

  @Test
  void create() {
    ExerciseDto expected = ExerciseDto.builder()
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.CORE.name().toLowerCase())
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();

    ExerciseDto savedExercise = service.create(expected);
    idsToDelete.add(savedExercise.getId());

    ExerciseDto actual = service.getOne(savedExercise.getId());
    assertThat(actual)
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(expected);
  }

  @Test
  void getAll() {
    List<Exercise> expected = new ArrayList<>();
    Exercise exercise1 = Exercise.builder()
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.BACK)
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    Exercise exercise2 = Exercise.builder()
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.CHEST)
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    expected.add(service.create(exercise1));
    expected.add(service.create(exercise2));
    expected.forEach(exercise -> idsToDelete.add(exercise.getId()));
    List<Exercise> actual = (List<Exercise>) service.getAll();
    assertThat(actual)
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
        .containsAll(expected);
  }

  @Test
  void getAllAsDto() {
    List<ExerciseDto> expected = new ArrayList<>();
    ExerciseDto exerciseDto1 = ExerciseDto.builder()
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.CORE.name().toLowerCase())
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    ExerciseDto exerciseDto2 = ExerciseDto.builder()
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.CORE.name().toLowerCase())
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    expected.add(service.create(exerciseDto1));
    expected.add(service.create(exerciseDto2));
    expected.forEach(exerciseDto -> idsToDelete.add(exerciseDto.getId()));

    List<ExerciseDto> actual = service.getAllAsDto();
    assertThat(actual)
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
        .containsAll(expected);
  }

  @Test
  void update() {
    ExerciseDto exerciseDto = ExerciseDto.builder()
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.BACK.name().toLowerCase())
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();

    ExerciseDto savedExercise = service.create(exerciseDto);
    idsToDelete.add(savedExercise.getId());

    ExerciseDto updatedExercise = ExerciseDto.builder()
        .id(savedExercise.getId())
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.CHEST.name().toLowerCase())
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    service.update(updatedExercise);
    ExerciseDto actual = service.getOne(savedExercise.getId());
    assertAll(
        () -> assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields("id", "muscleGroup")
            .isEqualTo(updatedExercise),
        () -> assertThat(actual.getMuscleGroup().toLowerCase())
            .isEqualTo(updatedExercise.getMuscleGroup().toLowerCase())
    );
  }

  @Test
  void exists() {
    ExerciseDto exerciseDto = ExerciseDto.builder()
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.CORE.name().toLowerCase())
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    ExerciseDto savedExercise = service.create(exerciseDto);
    idsToDelete.add(savedExercise.getId());

    assertThat(service.exists(savedExercise.getId())).isTrue();
  }

  @Test
  void delete() {
    ExerciseDto exerciseDto = ExerciseDto.builder()
        .name(new RandomString(8).nextString())
        .imageUrl(new RandomString(8).nextString())
        .muscleGroup(MuscleGroup.BACK.name().toLowerCase())
        .description(new RandomString(8).nextString())
        .videoUrl(new RandomString(8).nextString())
        .build();
    ExerciseDto savedExercise = service.create(exerciseDto);
    idsToDelete.add(savedExercise.getId());

    assertThat(service.exists(savedExercise.getId())).isTrue();
    service.delete(savedExercise.getId());
    assertThat(service.exists(savedExercise.getId())).isFalse();
  }
}
