package microservice.repository;

import java.util.List;
import microservice.entity.Exercise;
import microservice.entity.MuscleGroup;
import org.springframework.data.repository.CrudRepository;

public interface ExerciseRepository extends CrudRepository<Exercise, String> {

  List<Exercise> findByMuscleGroup(MuscleGroup muscleGroup);

  Exercise findByName(String name);
}
