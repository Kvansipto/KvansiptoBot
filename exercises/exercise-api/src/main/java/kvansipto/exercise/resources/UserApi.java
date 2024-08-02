package kvansipto.exercise.resources;

import kvansipto.exercise.dto.UserDto;

public interface UserApi {

  UserDto register(UserDto user);

  boolean exists(Long chatId);

  UserDto get(Long chatId);
}
