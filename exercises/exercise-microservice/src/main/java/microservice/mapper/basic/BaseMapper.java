package microservice.mapper.basic;

public interface BaseMapper<ENTITY, DTO> {

  ENTITY toEntity(DTO dto);

  DTO toDto(ENTITY entity);
}
