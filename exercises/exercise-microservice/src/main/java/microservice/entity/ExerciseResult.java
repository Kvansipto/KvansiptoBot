package microservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@Entity(name = "exercise_result")
@Valid
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseResult extends BaseEntity {

  @ManyToOne
  private Exercise exercise;
  private double weight;
  private byte numberOfSets;
  private byte numberOfRepetitions;
  private String comment;

  @PastOrPresent(message = "The date cannot be in the future")
  private LocalDate date;

  @ManyToOne
  private User user;
}
