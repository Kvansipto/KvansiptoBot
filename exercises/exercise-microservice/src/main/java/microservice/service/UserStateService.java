package microservice.service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserStateService {

  private static final String KEY = "UserState";
  private final HashOperations<String, Long, UserState> hashOperations;

  @Autowired
  public UserStateService(RedisTemplate<String, UserState> redisTemplate) {
    this.hashOperations = redisTemplate.opsForHash();
  }

  public Optional<UserState> getCurrentState(Long chatId) {
    return Optional.ofNullable(hashOperations.get(KEY, chatId));
  }

  public void setCurrentState(Long chatId, UserState userState) {
    hashOperations.put(KEY, chatId, userState);
    hashOperations.getOperations().expire(KEY, 24, TimeUnit.HOURS);
  }

  public void removeUserState(Long chatId) {
    hashOperations.delete(KEY, chatId);
  }
}
