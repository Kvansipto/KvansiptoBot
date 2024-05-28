package kvansipto.telegram.microservice.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import kvansipto.exercise.dto.ExerciseDto;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.dto.MuscleGroupDto;
import kvansipto.exercise.dto.UserDto;
import lombok.RequiredArgsConstructor;
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

  // Users

  public boolean userExists(String chatId) {
    return Boolean.TRUE.equals(
        restTemplate.getForEntity(String.format("http://localhost:8080/users/%s/exists", chatId), Boolean.class)
            .getBody());
  }

  public UserDto saveUser(UserDto user) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<UserDto> requestEntity = new HttpEntity<>(user, headers);
    ResponseEntity<UserDto> response = restTemplate.postForEntity("http://localhost:8080/users", requestEntity,
        UserDto.class);
    return response.getBody();
  }

  public UserDto getUser(String chatId) {
    ResponseEntity<UserDto> response = restTemplate.getForEntity(
        "http://localhost:8080/users/" + chatId, UserDto.class);
    return response.getBody();
  }

  // Exercises

  public List<ExerciseDto> getExercisesByMuscleGroup(MuscleGroupDto muscleGroup) {
    ResponseEntity<ExerciseDto[]> response = restTemplate.getForEntity(
        "http://localhost:8080/exercises?muscle-group=" + muscleGroup, ExerciseDto[].class);
    return Arrays.asList(Objects.requireNonNull(response.getBody()));
  }

  public ExerciseDto getExerciseByName(String exerciseName) {
    ResponseEntity<ExerciseDto> response = restTemplate.getForEntity(
        "http://localhost:8080/exercises?name=" + exerciseName, ExerciseDto.class);
    return response.getBody();
  }

  // ExerciseResults

  public boolean saveExerciseResult(ExerciseResultDto exerciseResultDto) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<ExerciseResultDto> requestEntity = new HttpEntity<>(exerciseResultDto, headers);
    ResponseEntity<ExerciseResultDto> response = restTemplate.postForEntity("http://localhost:8080/exercise-results",
        requestEntity,
        ExerciseResultDto.class);
    return response.getStatusCode().is2xxSuccessful();
  }

  public List<ExerciseResultDto> getExerciseResults(ExerciseDto exercise, String chatId) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    Map<String, ExerciseDto> body = new HashMap<>();
    body.put(chatId, exercise);
    HttpEntity<Map<String, ExerciseDto>> requestEntity = new HttpEntity<>(body, headers);
    ResponseEntity<ExerciseResultDto[]> response = restTemplate.postForEntity("http://localhost:8080/exercise-results/",
        requestEntity,
        ExerciseResultDto[].class);
    return Arrays.asList(Objects.requireNonNull(response.getBody()));
  }

  //MuscleGroups

  public List<MuscleGroupDto> getMuscleGroups() {
    ResponseEntity<MuscleGroupDto[]> response = restTemplate.getForEntity("http://localhost:8080/muscle-groups",
        MuscleGroupDto[].class);
    return Arrays.asList(Objects.requireNonNull(response.getBody()));
  }
}
