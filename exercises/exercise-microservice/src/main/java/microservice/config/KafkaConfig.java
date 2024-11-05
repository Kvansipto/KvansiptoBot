package microservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kvansipto.exercise.dto.UpdateDto;
import kvansipto.exercise.wrapper.BotApiMethodInterface;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
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
  @Value("${kafka.group.id.messages}")
  private String groupIdMessages;
  @Value("${kafka.topic.messages}")
  private String topicMessages;

  //producer MainMenuCommand
  @Bean
  public ReactiveKafkaProducerTemplate<String, List<BotCommand>> botCommandListReactiveSender() {
    Map<String, Object> config = new HashMap<>();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BotCommandListSerializer.class);
    SenderOptions<String, List<BotCommand>> senderOptions = SenderOptions.create(config);
    return new ReactiveKafkaProducerTemplate<>(senderOptions);
  }

  //producer BotApiMethodInterface
  @Bean
  public ReactiveKafkaProducerTemplate<Long, BotApiMethodInterface> botApiMethodReactiveSender() {
    Map<String, Object> config = new HashMap<>();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BotApiMethodSerializer.class);
    config.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 20971520);

    SenderOptions<Long, BotApiMethodInterface> senderOptions = SenderOptions.create(config);
    return new ReactiveKafkaProducerTemplate<>(senderOptions);
  }

  //consumer UpdateDto
  @Bean
  public ReactiveKafkaConsumerTemplate<Long, UpdateDto> updateDtoReactiveKafkaConsumerTemplate() {
    Map<String, Object> config = new HashMap<>();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ConsumerConfig.GROUP_ID_CONFIG, groupIdMessages);
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UpdateDtoDeserializer.class);
    ReceiverOptions<Long, UpdateDto> receiverOptions = ReceiverOptions.<Long, UpdateDto>create(config)
        .subscription(List.of(topicMessages));
    return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
  }

  public static class UpdateDtoDeserializer implements Deserializer<UpdateDto> {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public UpdateDto deserialize(String topic, byte[] data) {
      try {
        return objectMapper.readValue(data, UpdateDto.class);
      } catch (Exception e) {
        throw new RuntimeException("Ошибка при десериализации UpdateDto", e);
      }
    }
  }

  public static class BotCommandListSerializer implements Serializer<List<BotCommand>> {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public byte[] serialize(String topic, List<BotCommand> data) {
      try {
        return objectMapper.writeValueAsBytes(data);
      } catch (Exception e) {
        throw new RuntimeException("Ошибка при сериализации List<MainMenuCommand>", e);
      }
    }
  }

  public static class BotApiMethodSerializer implements Serializer<BotApiMethodInterface> {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

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
