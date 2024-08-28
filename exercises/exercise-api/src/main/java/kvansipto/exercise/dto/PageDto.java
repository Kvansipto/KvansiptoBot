package kvansipto.exercise.dto;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PageDto<T extends Serializable> extends BaseDto {

  private List<T> content;
  private int pageNumber;
  private int pageSize;
  private long totalElements;
}
