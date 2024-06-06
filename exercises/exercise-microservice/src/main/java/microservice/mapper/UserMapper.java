package microservice.mapper;

import kvansipto.exercise.dto.UserDto;
import microservice.entity.User;
import microservice.mapper.basic.BaseMapper;
import microservice.mapper.basic.MappingConfig;
import org.mapstruct.Mapper;

@Mapper(config = MappingConfig.class)
public interface UserMapper extends BaseMapper<User, UserDto> {

}
