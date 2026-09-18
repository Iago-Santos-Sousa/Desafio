package com.desafio.ingestion.shared;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {
  @Bean
  JacksonJsonMessageConverter rabbitMessageConverter() {
    return new JacksonJsonMessageConverter("com.desafio.ingestion.ingestion.messaging");
  }

  @Bean
  DirectExchange ingestionExchange() {
    return new DirectExchange("ingestion.exchange", true, false);
  }

  @Bean
  Queue ingestionQueue() {
    return QueueBuilder.durable("ingestion.jobs")
        .withArgument("x-dead-letter-exchange", "ingestion.dlx")
        .build();
  }

  @Bean
  Binding ingestionBinding(Queue ingestionQueue, DirectExchange ingestionExchange) {
    return BindingBuilder.bind(ingestionQueue).to(ingestionExchange).with("ingestion.jobs");
  }

  @Bean
  DirectExchange deadLetterExchange() {
    return new DirectExchange("ingestion.dlx");
  }
}
