package microservice.mapper;

import kvansipto.exercise.dto.ExerciseDto;
import microservice.entity.Exercise;
import microservice.mapper.basic.BaseMapper;
import microservice.mapper.basic.MappingConfig;
import org.mapstruct.Mapper;

@Mapper(config = MappingConfig.class, uses = {MuscleGroupMapper.class})
public interface ExerciseMapper extends BaseMapper<Exercise, ExerciseDto> {

}
