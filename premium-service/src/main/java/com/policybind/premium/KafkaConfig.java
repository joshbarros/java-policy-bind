package com.policybind.premium;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.policybind.common.Channels;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public NewTopic accountsTopic() {
        return TopicBuilder.name(Channels.ACCOUNTS).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic productsTopic() {
        return TopicBuilder.name(Channels.PRODUCTS).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic treatiesTopic() {
        return TopicBuilder.name(Channels.TREATIES).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic quotesTopic() {
        return TopicBuilder.name(Channels.QUOTES).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic ratingsTopic() {
        return TopicBuilder.name(Channels.RATINGS).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic capacityTopic() {
        return TopicBuilder.name(Channels.CAPACITY).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic referralsTopic() {
        return TopicBuilder.name(Channels.REFERRALS).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic sanctionsTopic() {
        return TopicBuilder.name(Channels.SANCTIONS).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic bindsTopic() {
        return TopicBuilder.name(Channels.BINDS).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic documentsTopic() {
        return TopicBuilder.name(Channels.DOCUMENTS).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic premiumsTopic() {
        return TopicBuilder.name(Channels.PREMIUMS).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic commissionsTopic() {
        return TopicBuilder.name(Channels.COMMISSIONS).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic endorsementsTopic() {
        return TopicBuilder.name(Channels.ENDORSEMENTS).partitions(1).replicas(1).build();
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(ObjectMapper objectMapper) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        JsonSerializer<Object> values = new JsonSerializer<>(objectMapper);
        values.setAddTypeInfo(true);
        return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), values);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory(ObjectMapper objectMapper) {
        JsonDeserializer<Object> values = new JsonDeserializer<>(objectMapper);
        values.addTrustedPackages("*");
        values.setUseTypeHeaders(true);
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "default");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), values);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setMissingTopicsFatal(false);
        return factory;
    }
}
