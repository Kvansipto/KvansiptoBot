package microservice.service;

import kvansipto.exercise.dto.UserDto;
import microservice.entity.User;
import microservice.mapper.UserMapper;
import microservice.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService extends
    BaseMappedService<User, UserDto, String, UserRepository, UserMapper> {

  protected UserService(UserRepository repository, UserMapper mapper) {
    super(repository, mapper);
  }

  @Override
  public UserDto create(UserDto userDto) {
    User user = mapper.toEntity(userDto);
    return mapper.toDto(repository.save(user));
  }
}
