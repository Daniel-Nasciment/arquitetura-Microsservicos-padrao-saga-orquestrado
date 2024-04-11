package br.com.orchestratorservice.orchestratorservice.config.kafka;

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

import static br.com.orchestratorservice.orchestratorservice.core.enums.ETopics.*;


@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConfiguration {

    private static final int REPLICAS_COUNT = 1;
    private static final int PARTITIONS_COUNT = 1;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffSet;

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
    public NewTopic buildStartSagaTopic(){
        return buildTopic(START_SAGA.getTopic());
    }
    @Bean
    public NewTopic buildBaseOrchestratorTopic(){
        return buildTopic(BASE_ORCHESTRATOR.getTopic());
    }
    @Bean
    public NewTopic buildFinishSucessTopic(){
        return buildTopic(FINISH_SUCCESS.getTopic());
    }
    @Bean
    public NewTopic buildFinishFailTopic(){
        return buildTopic(FINISH_FAIL.getTopic());
    }
    @Bean
    public NewTopic buildProductValidationSucessTopic(){
        return buildTopic(PRODUCT_VALIDATION_SUCESS.getTopic());
    }
    @Bean
    public NewTopic buildProductValidationFailTopic(){
        return buildTopic(PRODUCT_VALIDATION_FAIL.getTopic());
    }
    @Bean
    public NewTopic buildPaymentSuccessTopic(){
        return buildTopic(PAYMENT_SUCESS.getTopic());
    }
    @Bean
    public NewTopic buildPaymentFailTopic(){
        return buildTopic(PAYMENT_FAIL.getTopic());
    }
    @Bean
    public NewTopic buildInventorySucessTopic(){
        return buildTopic(INVENTORY_SUCESS.getTopic());
    }
    @Bean
    public NewTopic buildInventoryFailTopic(){
        return buildTopic(INVENTORY_FAIL.getTopic());
    }
    @Bean
    public NewTopic buildNotifyEndingTopic(){
        return buildTopic(NOTIFY_ENDING.getTopic());
    }

}
