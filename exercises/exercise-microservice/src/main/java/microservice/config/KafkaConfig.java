package microservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kvansipto.exercise.dto.UpdateDto;
import kvansipto.exercise.wrapper.BotApiMethodInterface;
import microservice.service.command.menu.MainMenuCommand;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class KafkaConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  //producer MainMenuCommand
  @Bean
  public ProducerFactory<String, List<MainMenuCommand>> botCommandListProducerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BotCommandListSerializer.class);
    return new DefaultKafkaProducerFactory<>(config);
  }

  @Bean
  public KafkaTemplate<String, List<MainMenuCommand>> botCommandListKafkaTemplate() {
    return new KafkaTemplate<>(botCommandListProducerFactory());
  }

  //producer BotApiMethodInterface
  @Bean
  public ProducerFactory<Long, BotApiMethodInterface> botApiMethodProducerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BotApiMethodSerializer.class);
    return new DefaultKafkaProducerFactory<>(config);
  }

  @Bean
  public KafkaTemplate<Long, BotApiMethodInterface> botApiMethodKafkaTemplate() {
    return new KafkaTemplate<>(botApiMethodProducerFactory());
  }

  //consumer UpdateDto
  @Bean
  public ConsumerFactory<Long, UpdateDto> updateDtoConsumerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ConsumerConfig.GROUP_ID_CONFIG, "update_dto_group");
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UpdateDtoDeserializer.class);
    return new DefaultKafkaConsumerFactory<>(config);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<Long, UpdateDto> updateDtoKafkaListenerFactory() {
    ConcurrentKafkaListenerContainerFactory<Long, UpdateDto> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(updateDtoConsumerFactory());
    return factory;
  }

  public static class UpdateDtoDeserializer implements Deserializer<UpdateDto> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public UpdateDto deserialize(String topic, byte[] data) {
      try {
        return objectMapper.readValue(data, UpdateDto.class);
      } catch (Exception e) {
        throw new RuntimeException("Ошибка при десериализации UpdateDto", e);
      }
    }
  }

  public static class BotCommandListSerializer implements Serializer<List<MainMenuCommand>> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, List<MainMenuCommand> data) {
      try {
        return objectMapper.writeValueAsBytes(data);
      } catch (Exception e) {
        throw new RuntimeException("Ошибка при сериализации List<MainMenuCommand>", e);
      }
    }
  }
  public static class BotApiMethodSerializer implements Serializer<BotApiMethodInterface> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, BotApiMethodInterface data) {
      try {
        return objectMapper.writeValueAsBytes(data);
      } catch (Exception e) {
        throw new RuntimeException("Ошибка при сериализации BotApiMethodInterface", e);
      }
    }
  }
}
