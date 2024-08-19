package kvansipto.telegram.microservice.services;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.dto.UserDto;
import kvansipto.exercise.filter.ExerciseResultFilter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class RestToExercises {

  private final KafkaTemplate<Long, ExerciseResultFilter> exerciseResultFilterKafkaTemplate;
  @Autowired
  private final KafkaConsumerService kafkaConsumerService;

  @Value("${kafka.topic.request}")
  private String exerciseResultTopicRequest;

  private final RestTemplate restTemplate;

  @Value("${exercises.url}")
  private String exercisesUrl;

  public boolean userExists(Long chatId) {
    return Boolean.TRUE.equals(
        restTemplate.getForEntity(String.format("%s/users/%s/exists", exercisesUrl, chatId), Boolean.class)
            .getBody());
  }

  public UserDto saveUser(UserDto user) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<UserDto> requestEntity = new HttpEntity<>(user, headers);
    ResponseEntity<UserDto> response = restTemplate.postForEntity(String.format("%s/users", exercisesUrl),
        requestEntity,
        UserDto.class);
    return response.getBody();
  }

  public UserDto getUser(Long chatId) {
    ResponseEntity<UserDto> response = restTemplate.getForEntity(
        String.format("%s/users/%s", exercisesUrl, chatId), UserDto.class);
    return response.getBody();
  }

  public List<ExerciseDto> getExercisesByMuscleGroup(String muscleGroup) {
    ResponseEntity<ExerciseDto[]> response = restTemplate.getForEntity(
        String.format("%s/exercises?muscleGroup=%s", exercisesUrl, muscleGroup), ExerciseDto[].class);
    List<ExerciseDto> exercises = Arrays.asList(Objects.requireNonNull(response.getBody()));
    System.out.println("Упражнения, полученные для группы мышц " + muscleGroup + ": " + exercises);
    return exercises;
  }

  public ExerciseDto getExerciseByName(String exerciseName) {
    ResponseEntity<ExerciseDto> response = restTemplate.getForEntity(
        String.format("%s/exercise?name=%s", exercisesUrl, exerciseName), ExerciseDto.class);
    return response.getBody();
  }

  public boolean saveExerciseResult(ExerciseResultDto exerciseResultDto) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<ExerciseResultDto> requestEntity = new HttpEntity<>(exerciseResultDto, headers);
    ResponseEntity<ExerciseResultDto> response = restTemplate.postForEntity(
        String.format("%s/exercise-results", exercisesUrl),
        requestEntity,
        ExerciseResultDto.class);
    return response.getStatusCode().is2xxSuccessful();
  }

  @SneakyThrows
  public List<ExerciseResultDto> getExerciseResults(ExerciseDto exercise, Long chatId) {
    ExerciseResultFilter body = ExerciseResultFilter.builder()
        .exerciseDto(exercise)
        .userChatId(chatId)
        .build();
    exerciseResultFilterKafkaTemplate.send(exerciseResultTopicRequest, chatId, body);
    log.info("sent to kafka topic: {}, {}", exerciseResultTopicRequest, body);
    List<ExerciseResultDto> result = kafkaConsumerService.waitForResponse(chatId).getContent();
    log.info("get exercise result: {}", result);

    result.sort(Comparator.comparing(ExerciseResultDto::getDate).reversed());
    return result;
  }

  public List<String> getMuscleGroups() {
    ResponseEntity<String[]> response = restTemplate.getForEntity(String.format("%s/muscle-groups", exercisesUrl),
        String[].class);
    return Arrays.asList(Objects.requireNonNull(response.getBody()));
  }
}
