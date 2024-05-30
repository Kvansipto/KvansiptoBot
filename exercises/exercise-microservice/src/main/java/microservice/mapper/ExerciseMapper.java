package microservice.mapper;

import kvansipto.exercise.dto.ExerciseDto;
import microservice.entity.Exercise;
import microservice.entity.MuscleGroup;
import microservice.mapper.basic.BaseMapper;
import microservice.mapper.basic.MappingConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MappingConfig.class)
public interface ExerciseMapper extends BaseMapper<Exercise, ExerciseDto> {

  @Mapping(source = "muscleGroup", target = "muscleGroup", qualifiedByName = "stringToEnum")
  Exercise toEntity(ExerciseDto dto);

  @Mapping(source = "muscleGroup", target = "muscleGroup", qualifiedByName = "enumToString")
  ExerciseDto toDto(Exercise exercise);

  @Named("stringToEnum")
  default MuscleGroup stringToEnum(String muscleGroup) {
    return MuscleGroup.valueOf(muscleGroup.toUpperCase());
  }

  @Named("enumToString")
  default String enumToString(MuscleGroup muscleGroup) {
    return muscleGroup.name().toLowerCase();
  }
}
