package microservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "exercise_result")
@Valid
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseResult extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "exercise_id")
  private Exercise exercise;

  private double weight;

  @Column(name = "number_of_sets")
  private byte numberOfSets;

  @Column(name = "number_of_repetitions")
  private byte numberOfRepetitions;

  @PastOrPresent(message = "The date cannot be in the future")
  private LocalDate date;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;
}
