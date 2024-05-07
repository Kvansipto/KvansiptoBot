package io.project.KvansiptoBot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "exercise_result")
public class ExerciseResult {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

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
}
