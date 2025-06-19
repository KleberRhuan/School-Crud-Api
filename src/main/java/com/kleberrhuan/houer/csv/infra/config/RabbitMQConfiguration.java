/* (C)2025 Kleber Rhuan */
package com.kleberrhuan.houer.csv.infra.config;

import com.kleberrhuan.houer.csv.domain.constants.CsvImportConstants.Exchanges;
import com.kleberrhuan.houer.csv.domain.constants.CsvImportConstants.Queues;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {

  @Bean
  public Queue csvImportQueue() {
    return QueueBuilder.durable(Queues.CSV_IMPORT_QUEUE).build();
  }

  @Bean
  public Queue csvNotificationQueue() {
    return QueueBuilder.durable(Queues.CSV_NOTIFICATION_QUEUE).build();
  }

  @Bean
  public DirectExchange csvImportExchange() {
    return new DirectExchange(Exchanges.CSV_IMPORT_EXCHANGE);
  }

  @Bean
  public DirectExchange csvNotificationExchange() {
    return new DirectExchange(Exchanges.CSV_NOTIFICATION_EXCHANGE);
  }

  @Bean
  public Binding csvImportBinding() {
    return BindingBuilder
      .bind(csvImportQueue())
      .to(csvImportExchange())
      .with(Queues.CSV_IMPORT_QUEUE);
  }

  @Bean
  public Binding csvNotificationBinding() {
    return BindingBuilder
      .bind(csvNotificationQueue())
      .to(csvNotificationExchange())
      .with(Queues.CSV_NOTIFICATION_QUEUE);
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
    ConnectionFactory connectionFactory,
    MessageConverter messageConverter
  ) {
    SimpleRabbitListenerContainerFactory factory =
      new SimpleRabbitListenerContainerFactory();

    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(messageConverter);
    factory.setConcurrentConsumers(2);
    factory.setMaxConcurrentConsumers(5);

    return factory;
  }
}
