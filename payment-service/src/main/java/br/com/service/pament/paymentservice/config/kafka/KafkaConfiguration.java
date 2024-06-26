package br.com.service.pament.paymentservice.config.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;

import java.util.Map;


@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {

    private static final int PARTITIONS_COUNT = 1;
    private static final int REPLICAS_COUNT = 1;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffSet;
    @Value("${spring.kafka.topic.payment-success}")
    private String paymentSuccessTopic;
    @Value("${spring.kafka.topic.payment-fail}")
    private String paymentFailTopic;
    @Value("${spring.kafka.topic.orchestrator}")
    private String orchestratorTopic;

    @Bean
    public ConsumerFactory<String, String> consumerFactory(){
        return new DefaultKafkaConsumerFactory<>(consumerProps());
    }

    private Map<String, Object> consumerProps() {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, groupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffSet
        );
    }

    @Bean
    public ProducerFactory<String, String> producerFactory(){
        return new DefaultKafkaProducerFactory<>(consumertProps());
    }

    private Map<String, Object> consumertProps() {
        return Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
        );
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory){
        return new KafkaTemplate<>(producerFactory);
    }

    private NewTopic buildTopic(String name){
        return TopicBuilder
                .name(name)
                .replicas(REPLICAS_COUNT)
                .partitions(PARTITIONS_COUNT)
                .build();
    }

    @Bean
    public NewTopic buildPaymentSuccessTopic(){
        return buildTopic(this.paymentSuccessTopic);
    }
    @Bean
    public NewTopic buildPaymentFailTopic(){
        return buildTopic(this.paymentFailTopic);
    }
    @Bean
    public NewTopic buildOrchestratorTopic(){
        return buildTopic(this.orchestratorTopic);
    }

}
