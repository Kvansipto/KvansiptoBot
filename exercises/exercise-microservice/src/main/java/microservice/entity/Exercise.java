package microservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "exercise")
public class Exercise extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String name;
  private String description;
  @Column(name = "video_url")
  private String videoUrl;
  @Column(name = "image_url")
  private String imageUrl;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, name = "muscle_group")
  private MuscleGroup muscleGroup;
}
