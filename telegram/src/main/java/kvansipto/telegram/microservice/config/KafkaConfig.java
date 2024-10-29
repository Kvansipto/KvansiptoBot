package kvansipto.telegram.microservice.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kvansipto.exercise.dto.UpdateDto;
import kvansipto.exercise.wrapper.BotApiMethodInterface;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

@Configuration
public class KafkaConfig {

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${kafka.topic.messages}")
  private String messagesToExercisesTopicName;

  @Value("${kafka.topic.actions}")
  private String actionsFromExercisesTopicName;

  @Value("${kafka.topic.main-menu-commands}")
  private String mainMenuCommandsTopicName;

  @Bean
  public NewTopic messagesToExercisesTopic() {
    return new NewTopic(messagesToExercisesTopicName, 3, (short) 3);
  }

  @Bean
  public NewTopic actionsFromExercisesTopic() {
    return new NewTopic(actionsFromExercisesTopicName, 3, (short) 3);
  }

  @Bean
  public NewTopic mainMenuCommandsFromExercisesTopic() {
    return new NewTopic(mainMenuCommandsTopicName, 3, (short) 3);
  }

  //producer UpdateDto
  @Bean
  public ProducerFactory<Long, UpdateDto> updateDtoProducerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, UpdateDtoSerializer.class);
    return new DefaultKafkaProducerFactory<>(config);
  }

  @Bean
  public KafkaTemplate<Long, UpdateDto> updateDtoKafkaTemplate() {
    return new KafkaTemplate<>(updateDtoProducerFactory());
  }

  //consumer BotCommand
  @Bean
  public ConsumerFactory<String, List<BotCommand>> botCommandListConsumerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ConsumerConfig.GROUP_ID_CONFIG, "main-menu-command-group");
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, BotCommandListDeserializer.class);
    return new DefaultKafkaConsumerFactory<>(config);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, List<BotCommand>> botCommandListKafkaListenerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, List<BotCommand>> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(botCommandListConsumerFactory());
    return factory;
  }

  //consumer BotApiMethodInterface
  @Bean
  public ConsumerFactory<Long, BotApiMethodInterface> botApiMethodConsumerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ConsumerConfig.GROUP_ID_CONFIG, "exercise_bot_group");
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, BotApiMethodDeserializer.class);
    return new DefaultKafkaConsumerFactory<>(config);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<Long, BotApiMethodInterface> botApiMethodKafkaListenerFactory() {
    ConcurrentKafkaListenerContainerFactory<Long, BotApiMethodInterface> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(botApiMethodConsumerFactory());
    return factory;
  }
  public static class BotApiMethodDeserializer implements Deserializer<BotApiMethodInterface> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public BotApiMethodInterface deserialize(String topic, byte[] data) {
      try {
        return objectMapper.readValue(data, BotApiMethodInterface.class);
      } catch (Exception e) {
        throw new RuntimeException("Ошибка при десериализации BotApiMethodInterface", e);
      }
    }
  }

  public static class BotCommandListDeserializer implements Deserializer<List<BotCommand>> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<BotCommand> deserialize(String topic, byte[] data) {
      try {
        return objectMapper.readValue(data, new TypeReference<List<BotCommand>>() {});
      } catch (Exception e) {
        throw new RuntimeException("Ошибка при десериализации List<BotCommand>", e);
      }
    }
  }

  public static class UpdateDtoSerializer implements Serializer<UpdateDto> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, UpdateDto data) {
      try {
        return objectMapper.writeValueAsBytes(data);
      } catch (Exception e) {
        throw new RuntimeException("Ошибка при сериализации UpdateDto", e);
      }
    }
  }
}
