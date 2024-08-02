package microservice.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;
import java.util.UUID;
import kvansipto.exercise.dto.ExerciseDto;
import microservice.entity.MuscleGroup;
import microservice.service.postgre.AbstractPostgreTestContainerTestBase;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ExtendWith(SpringExtension.class)
class ExerciseServiceIntegrationTest extends AbstractPostgreTestContainerTestBase {

  @Autowired
  private ExerciseService service;

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

    ExerciseDto actual = service.getOne(exerciseId);
    assertThat(expected)
        .usingRecursiveComparison()
        .withFailMessage(
            "Expecting ExerciseDto %s to be the same as the exerciseDto that was sent, but it was not", actual)
        .isEqualTo(actual);
  }
}
