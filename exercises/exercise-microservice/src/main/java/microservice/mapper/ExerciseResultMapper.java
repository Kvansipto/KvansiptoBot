package microservice.mapper;

import kvansipto.exercise.dto.ExerciseResultDto;
import microservice.entity.ExerciseResult;
import microservice.mapper.basic.BaseMapper;
import microservice.mapper.basic.MappingConfig;
import org.mapstruct.Mapper;

@Mapper(config = MappingConfig.class, uses = {ExerciseMapper.class, UserMapper.class})
public interface ExerciseResultMapper extends BaseMapper<ExerciseResult, ExerciseResultDto> {

}
