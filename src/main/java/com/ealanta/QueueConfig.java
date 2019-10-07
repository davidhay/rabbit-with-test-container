package com.ealanta;

import javax.annotation.PostConstruct;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueueConfig {
	
	private String TYPE_ONE = "type1";
	private String TYPE_TWO = "type2";
	
	@Autowired
	private AmqpAdmin admin;
	
	@Bean
    public Queue getQueueOne() {
        return new Queue("queue1", true, false, false);
    }	
	
	@Bean
    public Queue getQueueTwo() {
        return new Queue("queue2", true, false, false);
    }

	@Bean
	public TopicExchange getTopic() {
		return new TopicExchange("topic1", true, false);
	}
	
	@Bean
	public Binding getTopicToQ1Binding() {
			return BindingBuilder.bind(getQueueOne()).to(getTopic()).with(TYPE_ONE);
	}
	
	@Bean
	public Binding getTopicToQ2Binding() {
			return BindingBuilder.bind(getQueueTwo()).to(getTopic()).with(TYPE_TWO);
	}
	
	
	@PostConstruct
	public void init() {
		admin.purgeQueue(getQueueOne().getName());
		admin.purgeQueue(getQueueTwo().getName());
	}
}
