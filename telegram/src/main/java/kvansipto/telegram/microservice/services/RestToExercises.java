package kvansipto.telegram.microservice.services;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class RestToExercises {

  private final RestTemplate restTemplate;

  public boolean userExists(String chatId) {
    return Boolean.TRUE.equals(
        restTemplate.getForEntity(String.format("http://exercises:8080/users/%s/exists", chatId), Boolean.class)
            .getBody());
  }

  public UserDto saveUser(UserDto user) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<UserDto> requestEntity = new HttpEntity<>(user, headers);
    ResponseEntity<UserDto> response = restTemplate.postForEntity("http://exercises:8080/users", requestEntity,
        UserDto.class);
    return response.getBody();
  }

  public UserDto getUser(String chatId) {
    ResponseEntity<UserDto> response = restTemplate.getForEntity(
        "http://exercises:8080/users/" + chatId, UserDto.class);
    return response.getBody();
  }

  public List<ExerciseDto> getExercisesByMuscleGroup(String muscleGroup) {
    ResponseEntity<ExerciseDto[]> response = restTemplate.getForEntity(
        "http://exercises:8080/exercises?muscleGroup=" + muscleGroup, ExerciseDto[].class);
    List<ExerciseDto> exercises = Arrays.asList(Objects.requireNonNull(response.getBody()));
    System.out.println("Упражнения, полученные для группы мышц " + muscleGroup + ": " + exercises);
    return exercises;
  }

  public ExerciseDto getExerciseByName(String exerciseName) {
    ResponseEntity<ExerciseDto> response = restTemplate.getForEntity(
        "http://exercises:8080/exercise?name=" + exerciseName, ExerciseDto.class);
    return response.getBody();
  }

  public boolean saveExerciseResult(ExerciseResultDto exerciseResultDto) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<ExerciseResultDto> requestEntity = new HttpEntity<>(exerciseResultDto, headers);
    ResponseEntity<ExerciseResultDto> response = restTemplate.postForEntity("http://exercises:8080/exercise-results",
        requestEntity,
        ExerciseResultDto.class);
    return response.getStatusCode().is2xxSuccessful();
  }

  public List<ExerciseResultDto> getExerciseResults(ExerciseDto exercise, String chatId) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    Pair<ExerciseDto, String> body = Pair.of(exercise, chatId);
    HttpEntity<Pair<ExerciseDto, String>> requestEntity = new HttpEntity<>(body, headers);
    ResponseEntity<ExerciseResultDto[]> response = restTemplate.postForEntity("http://exercises:8080/exercise-results/",
        requestEntity,
        ExerciseResultDto[].class);
    return Arrays.asList(Objects.requireNonNull(response.getBody()));
  }

  public List<String> getMuscleGroups() {
    ResponseEntity<String[]> response = restTemplate.getForEntity("http://exercises:8080/muscle-groups",
        String[].class);
    return Arrays.asList(Objects.requireNonNull(response.getBody()));
  }
}
