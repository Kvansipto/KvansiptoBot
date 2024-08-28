//package kvansipto.exercise.kafka;
//
//import java.util.HashMap;
//import java.util.Map;
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.common.serialization.LongDeserializer;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//import org.springframework.kafka.config.KafkaListenerContainerFactory;
//import org.springframework.kafka.core.ConsumerFactory;
//import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
//import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
//import org.springframework.kafka.support.serializer.JsonDeserializer;
//
//@Configuration
//@EnableKafka
//public class KafkaConsumerConfig {
//
//  @Value("${kafka.server}")
//  private String kafkaServer;
//
//  @Value("${kafka.group.id}")
//  private String kafkaGroupId;
//
//  @Bean
//  public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Long, Object>> kafkaListenerContainerFactory() {
//    ConcurrentKafkaListenerContainerFactory<Long, Object> factory =
//        new ConcurrentKafkaListenerContainerFactory<>();
//    factory.setConsumerFactory(consumerFactory());
//    return factory;
//  }
//
//  @Bean
//  public ConsumerFactory<Long, Object> consumerFactory() {
//    Map<String, Object> props = new HashMap<>();
//    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
//    props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaGroupId);
//
//    JsonDeserializer<Object> deserializer = new JsonDeserializer<>();
//    deserializer.addTrustedPackages("kvansipto.exercise.filter", "kvansipto.exercise.dto");
//
//    return new DefaultKafkaConsumerFactory<>(props, new LongDeserializer(), deserializer);
//  }
//
////  TODO Почему не видит этот bean?
////  @Bean(name = "exerciseKafkaListenerContainerFactory")
////  public ConcurrentKafkaListenerContainerFactory<Long, List<ExerciseResultDto>> exerciseKafkaListenerContainerFactory() {
////    ConcurrentKafkaListenerContainerFactory<Long, List<ExerciseResultDto>> factory =
////        new ConcurrentKafkaListenerContainerFactory<>();
////    factory.setConsumerFactory(exerciseConsumerFactory());
////    return factory;
////  }
//}
