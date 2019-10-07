package com.ealanta;

import javax.annotation.PostConstruct;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SendingService {

	@Autowired
	private RabbitTemplate rabbit;

	@Autowired
	private TopicExchange exchange;

	public void sendMessage(String type, String messageBody) {
		try {
			String routingKey = type;
			rabbit.convertAndSend(exchange.getName(), routingKey, messageBody);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
