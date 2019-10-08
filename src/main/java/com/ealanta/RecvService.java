package com.ealanta;

import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RecvService {

	private final List<String> type1msgs = new ArrayList<>();
	
	private final List<String> type2msgs = new ArrayList<>();
		
	@RabbitListener(queues = QueueConfig.QUEUE_ONE)
	public void receiveType1(String msg) {
		log.info("recvd[{}]from[{}]", msg, QueueConfig.QUEUE_ONE);
		type1msgs.add(msg);
	}
	
	@RabbitListener(queues = QueueConfig.QUEUE_TWO)
	public void receiveType2(String msg) {
		log.info("recvd[{}]from[{}]", msg, QueueConfig.QUEUE_TWO);
		type2msgs.add(msg);
	}
		
	public List<String> getType1Messages(){
		return type1msgs;
	}
	
	public List<String> getType2Messages(){
		return type2msgs;
	}
}
