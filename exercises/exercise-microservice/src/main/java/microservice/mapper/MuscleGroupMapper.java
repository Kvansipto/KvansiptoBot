package microservice.mapper;

import kvansipto.exercise.dto.MuscleGroupDto;
import microservice.entity.MuscleGroup;
import microservice.mapper.basic.BaseMapper;
import microservice.mapper.basic.MappingConfig;
import org.mapstruct.Mapper;

@Mapper(config = MappingConfig.class)
public interface MuscleGroupMapper extends BaseMapper<MuscleGroup, MuscleGroupDto> {

}
