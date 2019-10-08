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
	public void receiveType1(Message rbtMessages) {
		String msg = extractBody(rbtMessages);
		System.out.printf("recvd[%s]from[%s]%n", msg, "queue1");
		type1msgs.add(msg);
	}
	
	@RabbitListener(queues = "queue2")
	public void receiveType2(Message rbtMessages) {
		String msg = extractBody(rbtMessages);
		System.out.printf("recvd[%s]from[%s]%n", msg, "queue2");
		type2msgs.add(msg);
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
