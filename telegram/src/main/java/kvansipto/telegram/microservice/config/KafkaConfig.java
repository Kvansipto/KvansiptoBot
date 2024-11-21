package kvansipto.telegram.microservice.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kvansipto.exercise.dto.UpdateDto;
import kvansipto.exercise.wrapper.BotApiMethodInterface;
import kvansipto.exercise.wrapper.BotApiMethodWrapper;
import kvansipto.exercise.wrapper.DeleteMessagesWrapper;
import kvansipto.exercise.wrapper.EditMessageWrapper;
import kvansipto.exercise.wrapper.SendMessageWrapper;
import kvansipto.exercise.wrapper.SendPhotoWrapper;
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
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

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
  @Value("${kafka.group.id.main-menu-commands}")
  private String mainMenuCommandsGroupId;
  @Value("${kafka.group.id.actions}")
  private String actionsGroupId;
  @Value("${kafka.topic.media}")
  private String mediaTopicName;
  @Value("${kafka.group.id.media}")
  private String mediaGroupId;

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

  @Bean
  public NewTopic mediaFromExercisesTopic() {
    return new NewTopic(mediaTopicName, 3, (short) 3);
  }

  @Bean
  public ReactiveKafkaConsumerTemplate<Long, String> mediaReactiveKafkaConsumerTemplate() {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ConsumerConfig.GROUP_ID_CONFIG, mediaGroupId);
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    ReceiverOptions<Long, String> receiverOptions = ReceiverOptions.<Long, String>create(config)
        .subscription(List.of(mediaTopicName));
    return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
  }

  @Bean
  public ReactiveKafkaProducerTemplate<Long, UpdateDto> updateDtoReactiveSender() {
    Map<String, Object> config = new HashMap<>();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, UpdateDtoSerializer.class);
    SenderOptions<Long, UpdateDto> senderOptions = SenderOptions.create(config);
    return new ReactiveKafkaProducerTemplate<>(senderOptions);
  }

  @Bean
  public ReactiveKafkaConsumerTemplate<String, List<BotCommand>> botCommandReactiveKafkaConsumerTemplate() {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ConsumerConfig.GROUP_ID_CONFIG, mainMenuCommandsGroupId);
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, BotCommandListDeserializer.class);
    ReceiverOptions<String, List<BotCommand>> receiverOptions = ReceiverOptions.<String, List<BotCommand>>create(config)
        .subscription(List.of(mainMenuCommandsTopicName));
    return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
  }

  @Bean
  public ReactiveKafkaConsumerTemplate<Long, BotApiMethodInterface> botApiMethodReactiveKafkaConsumerTemplate() {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ConsumerConfig.GROUP_ID_CONFIG, actionsGroupId);
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, BotApiMethodDeserializer.class);
    ReceiverOptions<Long, BotApiMethodInterface> receiverOptions = ReceiverOptions.<Long, BotApiMethodInterface>create(config)
        .subscription(List.of(actionsFromExercisesTopicName));
    return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
  }

  public static class BotApiMethodDeserializer implements Deserializer<BotApiMethodInterface> {
    private final ObjectMapper objectMapper;

  public BotApiMethodDeserializer() {
    objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.registerSubtypes(
        new NamedType(BotApiMethodWrapper.class, "BotApiMethodWrapper"),
        new NamedType(SendMessageWrapper.class, "SendMessageWrapper"),
        new NamedType(EditMessageWrapper.class, "EditMessageWrapper"),
        new NamedType(SendPhotoWrapper.class, "SendPhotoWrapper"),
        new NamedType(DeleteMessagesWrapper.class, "DeleteMessageWrapper")
    );
  }

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
