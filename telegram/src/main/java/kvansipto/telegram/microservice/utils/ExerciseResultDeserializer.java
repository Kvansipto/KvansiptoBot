package kvansipto.telegram.microservice.utils;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import kvansipto.exercise.dto.ExerciseResultDto;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.List;
import java.util.Map;

public class ExerciseResultDeserializer implements Deserializer<List<ExerciseResultDto>> {

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
  }

  @Override
  public List<ExerciseResultDto> deserialize(String topic, byte[] data) {
    if (data == null || data.length == 0) {
      return null;
    }

    try {
      return objectMapper.readValue(data, new TypeReference<List<ExerciseResultDto>>() {});
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public List<ExerciseResultDto> deserialize(String topic, Headers headers, byte[] data) {
    return deserialize(topic, data);
  }

  @Override
  public void close() {
  }
}
