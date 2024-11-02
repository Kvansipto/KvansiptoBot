package microservice.service.user.state;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserStateService {

  private static final String KEY = "UserState";
  private final HashOperations<String, Long, UserState> hashOperations;

  @Autowired
  public UserStateService(RedisTemplate<String, UserState> redisTemplate) {
    this.hashOperations = redisTemplate.opsForHash();
  }

  public Optional<UserState> getCurrentState(Long chatId) {
    Optional<UserState> userState = Optional.ofNullable(hashOperations.get(KEY, chatId));
    log.info("User with id {} has current state: {}", chatId, userState);
    return userState;
  }

  public void setCurrentState(Long chatId, UserState userState) {
    hashOperations.put(KEY, chatId, userState);
    log.info("Set state for user with id {} to {}", chatId, userState);
    hashOperations.getOperations().expire(KEY, 24, TimeUnit.HOURS);
  }

  public void removeUserState(Long chatId) {
    hashOperations.delete(KEY, chatId);
    log.info("Remove state for user with id {}", chatId);
  }
}
