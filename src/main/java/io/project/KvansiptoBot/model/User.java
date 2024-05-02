package io.project.KvansiptoBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.sql.Timestamp;
import lombok.Data;

@Data
@Entity(name = "userDataTable")
public class User {

  @Id
  private Long chatId;

  private String userName;
  private String firstName;
  private String lastName;
  private Timestamp registeredAt;
}
