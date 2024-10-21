package kvansipto.telegram.microservice.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.HashMap;
import java.util.Map;
import kvansipto.exercise.dto.ExerciseResultDto;
import kvansipto.exercise.dto.PageDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
public class KafkaConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  @Bean
  public JsonDeserializer<PageDto<ExerciseResultDto>> exerciseResultDtoDeserializer(ObjectMapper objectMapper) {
    // Using TypeReference to handle generics properly
    JsonDeserializer<PageDto<ExerciseResultDto>> deserializer = new JsonDeserializer<>(
        new TypeReference<>() {
        }, objectMapper, false);

    Map<String, Object> config = new HashMap<>();
    config.put(JsonDeserializer.TRUSTED_PACKAGES, "kvansipto.exercise.dto");
    deserializer.configure(config, false);

    return deserializer;
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<Long, PageDto<ExerciseResultDto>> kafkaListenerContainerFactory(
      ConsumerFactory<Long, PageDto<ExerciseResultDto>> consumerFactory) {
    ConcurrentKafkaListenerContainerFactory<Long, PageDto<ExerciseResultDto>> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    return factory;
  }

  @Bean
  public ConsumerFactory<Long, PageDto<ExerciseResultDto>> consumerFactory(
      JsonDeserializer<PageDto<ExerciseResultDto>> deserializer) {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "server.exercise.results");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer.getClass());

    return new DefaultKafkaConsumerFactory<>(props, new LongDeserializer(), deserializer);
  }
}
