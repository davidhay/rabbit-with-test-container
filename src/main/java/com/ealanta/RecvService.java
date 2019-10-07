package com.ealanta;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RecvService {

	private final List<String> type1msgs = new ArrayList<>();
	
	private final List<String> type2msgs = new ArrayList<>();
		
	@RabbitListener(queues = "queue1")
	public void receiveType1(Message msg) {
		type1msgs.add(extractBody(msg));
	}
	
	@RabbitListener(queues = "queue2")
	public void receiveType2(Message msg) {
		type2msgs.add(extractBody(msg));
	}
	
	private String extractBody(Message msg) {
		return new String(msg.getBody(),StandardCharsets.UTF_8);
	}
	
	public List<String> getType1Messages(){
		return type1msgs;
	}
	
	public List<String> getType2Messages(){
		return type2msgs;
	}

}
