package com.ealanta;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SendingService {

	@Autowired
	private RabbitTemplate rabbit;

	public void sendMessage(String type, String messageBody) {
		try {
			String routingKey = type;
			rabbit.convertAndSend(RabbitInfo.TOPIC_ONE, routingKey, messageBody);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
