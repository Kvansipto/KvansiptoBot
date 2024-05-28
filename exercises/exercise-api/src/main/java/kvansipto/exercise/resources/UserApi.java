package kvansipto.exercise.resources;

import kvansipto.exercise.dto.UserDto;

public interface UserApi {

  UserDto register(UserDto user);

  boolean exists(String chatId);

  UserDto get(String chatId);
}
