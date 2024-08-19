package kvansipto.telegram.microservice.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Map;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.dto.PageDto;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

public class ExerciseResultDeserializer implements Deserializer<PageDto<ExerciseResultDto>> {

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
  }

  @Override
  public PageDto<ExerciseResultDto> deserialize(String topic, byte[] data) {
    if (data == null || data.length == 0) {
      return null;
    }

    try {
      return objectMapper.readValue(data, new TypeReference<>() {
      });
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public PageDto<ExerciseResultDto> deserialize(String topic, Headers headers, byte[] data) {
    return deserialize(topic, data);
  }

  @Override
  public void close() {
  }
}
