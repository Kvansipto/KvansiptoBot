package microservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.io.Serializable;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@Entity(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

  @Id
  private Long id;
  private String userName;
  private String firstName;
  private String lastName;
  private Timestamp registeredAt;
}
