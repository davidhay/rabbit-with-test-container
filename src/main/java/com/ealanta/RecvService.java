package com.ealanta;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RecvService {

	private final Logger log = LoggerFactory.getLogger(RecvService.class);

	private final List<String> type1msgs = new ArrayList<>();
	
	private final List<String> type2msgs = new ArrayList<>();
		
	@RabbitListener(queues = RabbitInfo.QUEUE_ONE)
	public void receiveType1(String msg) {
		log.info("recvd[{}]from[{}]", msg, RabbitInfo.QUEUE_ONE);
		type1msgs.add(msg);
	}
	
	@RabbitListener(queues = RabbitInfo.QUEUE_TWO)
	public void receiveType2(String msg) {
		log.info("recvd[{}]from[{}]", msg, RabbitInfo.QUEUE_TWO);
		type2msgs.add(msg);
	}
		
	public List<String> getType1Messages(){
		return type1msgs;
	}
	
	public List<String> getType2Messages(){
		return type2msgs;
	}
}
