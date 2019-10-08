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
	
	public static final String TYPE_ONE = "type1";
	public static final String TYPE_TWO = "type2";
	public static final String QUEUE_ONE = "queue1";
	public static final String QUEUE_TWO = "queue2";
	public static final String TOPIC_ONE = "topic1";
	
	@Autowired
	private AmqpAdmin admin;
	
	@Bean
    public Queue getQueueOne() {
        return new Queue(QUEUE_ONE, true, false, false);
    }	
	
	@Bean
    public Queue getQueueTwo() {
        return new Queue(QUEUE_TWO, true, false, false);
    }

	@Bean
	public TopicExchange getTopic() {
		return new TopicExchange(TOPIC_ONE, true, false);
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
