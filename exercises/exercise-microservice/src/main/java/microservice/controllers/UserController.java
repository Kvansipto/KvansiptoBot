package microservice.controllers;

import kvansipto.exercise.dto.UserDto;
import kvansipto.exercise.resources.UserApi;
import microservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController implements UserApi {

  @Autowired
  UserService service;

  @Override
  @PostMapping("/users")
  public UserDto register(@RequestBody UserDto user) {
    return service.create(user);
  }

  @Override
  @GetMapping("/users/{id}/exists")
  public boolean exists(@PathVariable("id") String chatId) {
    return service.exists(chatId);
  }

  @Override
  @GetMapping("/users/{id}")
  public UserDto get(@PathVariable("id") String chatId) {
    return service.getOne(chatId);
  }
}
