package io.project.kvansiptobot.repository;

import io.project.kvansiptobot.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {

}
