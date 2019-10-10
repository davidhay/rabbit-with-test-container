package com.ealanta;

import static com.ealanta.QueueConfig.QUEUE_ONE;
import static com.ealanta.QueueConfig.QUEUE_TWO;
import static com.ealanta.QueueConfig.TOPIC_ONE;
import static com.ealanta.QueueConfig.TYPE_ONE;
import static com.ealanta.QueueConfig.TYPE_TWO;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
public class SendRecvTestUsingRabbitInTestContainerTest extends BaseRabbitInTestContainerTest {

	@Autowired
	private RecvService recvService;

	@Autowired
	private SendingService sendService;	
	
	@BeforeClass
	public static void initRabbit() {
		try {
			String result = runCommandInDocker("rabbitmqadmin --vhost=/ import /rabbit-config-for-test.json");
			System.out.printf("ls [%s]", result);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

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
		System.out.printf("queues[%s]%n",result);
		Assert.assertEquals(QUEUE_ONE + " " + QUEUE_TWO + " test.queue.one", result);
	}
	
	@Test
	public void testExchangesInTestContainer() throws Exception {
		String result = runCommandInDocker("rabbitmqadmin list exchanges -f bash | xargs -n1 | grep -v amq");
		Assert.assertEquals(TOPIC_ONE, result);
	}

}
