package microservice.repository;

import java.util.List;
import microservice.entity.Exercise;
import microservice.entity.MuscleGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, String> {

  List<Exercise> findByMuscleGroup(MuscleGroup muscleGroup);

  Exercise findByName(String name);
}
