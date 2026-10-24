package com.policybind.sanction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.policybind.common.Channels;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class QueueConfig {

    @Bean
    public Queue rateDlq() {
        return QueueBuilder.durable(Channels.RATE_DLQ).build();
    }

    @Bean
    @DependsOn("rateDlq")
    public Queue rateQueue() {
        return QueueBuilder.durable(Channels.RATE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.RATE_DLQ)
                .build();
    }

    @Bean
    public Queue holdDlq() {
        return QueueBuilder.durable(Channels.HOLD_DLQ).build();
    }

    @Bean
    @DependsOn("holdDlq")
    public Queue holdQueue() {
        return QueueBuilder.durable(Channels.HOLD)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.HOLD_DLQ)
                .build();
    }

    @Bean
    public Queue releaseDlq() {
        return QueueBuilder.durable(Channels.RELEASE_DLQ).build();
    }

    @Bean
    @DependsOn("releaseDlq")
    public Queue releaseQueue() {
        return QueueBuilder.durable(Channels.RELEASE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.RELEASE_DLQ)
                .build();
    }

    @Bean
    public Queue commitDlq() {
        return QueueBuilder.durable(Channels.COMMIT_DLQ).build();
    }

    @Bean
    @DependsOn("commitDlq")
    public Queue commitQueue() {
        return QueueBuilder.durable(Channels.COMMIT)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.COMMIT_DLQ)
                .build();
    }

    @Bean
    public Queue returnDlq() {
        return QueueBuilder.durable(Channels.RETURN_CAPACITY_DLQ).build();
    }

    @Bean
    @DependsOn("returnDlq")
    public Queue returnQueue() {
        return QueueBuilder.durable(Channels.RETURN_CAPACITY)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.RETURN_CAPACITY_DLQ)
                .build();
    }

    @Bean
    public Queue screenDlq() {
        return QueueBuilder.durable(Channels.SCREEN_DLQ).build();
    }

    @Bean
    @DependsOn("screenDlq")
    public Queue screenQueue() {
        return QueueBuilder.durable(Channels.SCREEN)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.SCREEN_DLQ)
                .build();
    }

    @Bean
    public Queue decideDlq() {
        return QueueBuilder.durable(Channels.DECIDE_DLQ).build();
    }

    @Bean
    @DependsOn("decideDlq")
    public Queue decideQueue() {
        return QueueBuilder.durable(Channels.DECIDE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.DECIDE_DLQ)
                .build();
    }

    @Bean
    public Queue issueDlq() {
        return QueueBuilder.durable(Channels.ISSUE_DLQ).build();
    }

    @Bean
    @DependsOn("issueDlq")
    public Queue issueQueue() {
        return QueueBuilder.durable(Channels.ISSUE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.ISSUE_DLQ)
                .build();
    }

    @Bean
    public Queue bindCancelDlq() {
        return QueueBuilder.durable(Channels.BIND_CANCEL_DLQ).build();
    }

    @Bean
    @DependsOn("bindCancelDlq")
    public Queue bindCancelQueue() {
        return QueueBuilder.durable(Channels.BIND_CANCEL)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.BIND_CANCEL_DLQ)
                .build();
    }

    @Bean
    public Queue documentDlq() {
        return QueueBuilder.durable(Channels.DOCUMENT_DLQ).build();
    }

    @Bean
    @DependsOn("documentDlq")
    public Queue documentQueue() {
        return QueueBuilder.durable(Channels.DOCUMENT)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.DOCUMENT_DLQ)
                .build();
    }

    @Bean
    public Queue documentVoidDlq() {
        return QueueBuilder.durable(Channels.DOCUMENT_VOID_DLQ).build();
    }

    @Bean
    @DependsOn("documentVoidDlq")
    public Queue documentVoidQueue() {
        return QueueBuilder.durable(Channels.DOCUMENT_VOID)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.DOCUMENT_VOID_DLQ)
                .build();
    }

    @Bean
    public Queue postDlq() {
        return QueueBuilder.durable(Channels.POST_DLQ).build();
    }

    @Bean
    @DependsOn("postDlq")
    public Queue postQueue() {
        return QueueBuilder.durable(Channels.POST)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.POST_DLQ)
                .build();
    }

    @Bean
    public Queue premiumReturnDlq() {
        return QueueBuilder.durable(Channels.PREMIUM_RETURN_DLQ).build();
    }

    @Bean
    @DependsOn("premiumReturnDlq")
    public Queue premiumReturnQueue() {
        return QueueBuilder.durable(Channels.PREMIUM_RETURN)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.PREMIUM_RETURN_DLQ)
                .build();
    }

    @Bean
    public Queue premiumVoidDlq() {
        return QueueBuilder.durable(Channels.PREMIUM_VOID_DLQ).build();
    }

    @Bean
    @DependsOn("premiumVoidDlq")
    public Queue premiumVoidQueue() {
        return QueueBuilder.durable(Channels.PREMIUM_VOID)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.PREMIUM_VOID_DLQ)
                .build();
    }

    @Bean
    public Queue accrueDlq() {
        return QueueBuilder.durable(Channels.ACCRUE_DLQ).build();
    }

    @Bean
    @DependsOn("accrueDlq")
    public Queue accrueQueue() {
        return QueueBuilder.durable(Channels.ACCRUE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.ACCRUE_DLQ)
                .build();
    }

    @Bean
    public Queue reverseDlq() {
        return QueueBuilder.durable(Channels.REVERSE_DLQ).build();
    }

    @Bean
    @DependsOn("reverseDlq")
    public Queue reverseQueue() {
        return QueueBuilder.durable(Channels.REVERSE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.REVERSE_DLQ)
                .build();
    }

    @Bean
    public Queue endorseDlq() {
        return QueueBuilder.durable(Channels.ENDORSE_DLQ).build();
    }

    @Bean
    @DependsOn("endorseDlq")
    public Queue endorseQueue() {
        return QueueBuilder.durable(Channels.ENDORSE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(Channels.ENDORSE_DLQ)
                .build();
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("*");
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
