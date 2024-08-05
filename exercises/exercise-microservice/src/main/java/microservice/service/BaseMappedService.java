package microservice.service;

import jakarta.persistence.EntityNotFoundException;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import kvansipto.exercise.dto.BaseDto;
import microservice.entity.BaseEntity;
import microservice.mapper.basic.BaseMapper;
import org.springframework.data.jpa.repository.JpaRepository;

public abstract class BaseMappedService<E extends BaseEntity,
    D extends BaseDto,
    I extends Serializable,
    R extends JpaRepository<E, I>,
    M extends BaseMapper<E, D>> {

  R repository;
  M mapper;

  protected BaseMappedService(R repository, M mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  public D toDto(E entity) {
    return mapper.toDto(entity);
  }

  public E toEntity(D dto) {
    return mapper.toEntity(dto);
  }

  public E create(E entity) {
//    if (entity.getId() == null) {
//      entity.setId(UUID.randomUUID().toString());
//    }
    return save(entity);
  }

  public D create(D dto) {
    return toDto(create(toEntity(dto)));
  }

  private E save(E entity) {
    return repository.save(entity);
  }

  private Optional<E> findById(I id) {
    return repository.findById(id);
  }

  private Optional<D> findOne(I id) {
    return findById(id).map(this::toDto);
  }

  public D getOne(I id) {
    return toDto(getByIdOrThrow(id));
  }

  private E getByIdOrThrow(I id) {
    return findById(id)
        .orElseThrow(() -> new EntityNotFoundException(String.format("Entity with id %s not found", id)));
  }

  public Iterable<E> getAll() {
    return repository.findAll();
  }

  public List<D> getAllAsDto() {
    return StreamSupport.stream(getAll().spliterator(), false)
        .map(this::toDto)
        .toList();
  }

  public E update(E entity) {
    return save(entity);
  }

  public D update(D dto) {
    getByIdOrThrow((I) dto.getId());
    E updatedDto = toEntity(dto);
    E domain = update(updatedDto);
    return toDto(domain);
  }

  public boolean exists(I id) {
    return findById(id).isPresent();
  }

  public void delete(I id) {
    getByIdOrThrow(id);
    repository.deleteById(id);
  }
}
