package microservice.entity;

import jakarta.persistence.Entity;
import java.sql.Timestamp;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@Entity(name = "users")
public class User extends BaseEntity {

  private String userName;
  private String firstName;
  private String lastName;
  private Timestamp registeredAt;

  @Builder
  public User(Long id, String userName, String firstName, String lastName, Timestamp registeredAt) {
    super(id);
    this.userName = userName;
    this.firstName = firstName;
    this.lastName = lastName;
    this.registeredAt = registeredAt;
  }
}
