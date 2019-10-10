package com.ealanta;

import static com.ealanta.RabbitInfo.QUEUE_ONE;
import static com.ealanta.RabbitInfo.QUEUE_TWO;
import static com.ealanta.RabbitInfo.TOPIC_ONE;
import static com.ealanta.RabbitInfo.TYPE_ONE;
import static com.ealanta.RabbitInfo.TYPE_TWO;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
public class SendRecvTestUsingRabbitInTestContainerTest extends BaseRabbitInTestContainerTest {

	private static final Logger LOG = LoggerFactory.getLogger(SendRecvTestUsingRabbitInTestContainerTest.class);
	
	@Autowired
	private RecvService recvService;

	@Autowired
	private SendingService sendService;	
	
	@Test
	public void testSendRecv() throws UnsupportedOperationException, IOException, InterruptedException {
		
		String type1msg = "type1 test message";
		String type2msg = "type2 test message";
		sendService.sendMessage(TYPE_ONE, type1msg);
		sendService.sendMessage(TYPE_TWO, type2msg);

		TimeUnit.SECONDS.sleep(1);

		List<String> expected1 = Arrays.asList(type1msg);
		List<String> expected2 = Arrays.asList(type2msg);
		Assert.assertEquals(expected1, this.recvService.getType1Messages());
		Assert.assertEquals(expected2, this.recvService.getType2Messages());
	}

	@Test
	public void testCheckQueuesInTestContainer() throws Exception {
		String result = runCommandInDocker("rabbitmqadmin list queues -f bash");
		LOG.info("queues[{}]",result);
		Assert.assertEquals(QUEUE_ONE + " " + QUEUE_TWO, result);
	}
	
	@Test
	public void testExchangesInTestContainer() throws Exception {
		String result = runCommandInDocker("rabbitmqadmin list exchanges -f bash | xargs -n1 | grep -v amq");
		Assert.assertEquals(TOPIC_ONE, result);
	}

}
