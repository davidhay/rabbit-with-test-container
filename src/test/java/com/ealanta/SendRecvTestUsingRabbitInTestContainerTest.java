package com.ealanta;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.RabbitMQContainer;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=RabbitWithTestContainerApplication.class)
@ContextConfiguration(initializers = { SendRecvTestUsingRabbitInTestContainerTest.Initializer.class })
@Slf4j
@ActiveProfiles("test")
public class SendRecvTestUsingRabbitInTestContainerTest {

	@ClassRule
	public static RabbitMQContainer rabbit = new RabbitMQContainer();

	//helps check that Spring is using overridden property value from rabbit test container
	@Value("${spring.rabbitmq.username}")
	private String springPropUsername;
	
	//helps check that Spring is using overridden property value from rabbit test container
	@Value("${spring.rabbitmq.password}")
	private String springPropPassword;
	
	//helps check that Spring is using overridden property value from rabbit test container
	@Value("${spring.rabbitmq.host}")
	private String springPropHost;

	//helps check that Spring is using overridden property value from rabbit test container
	@Value("${spring.rabbitmq.port}")
	private Integer springPropPort;
	
	//helps check that Spring is using overridden property value from rabbit test container
	@Value("${spring.rabbitmq.virtual-host}")
	private String springPropVirtualHost;

	@Autowired
	private RecvService recvService;

	@Autowired
	private SendingService sendService;
	
	@Test
	public void testTestContainerPropsPickedUpBySpring() {
		
		int rabbitPort = rabbit.getAmqpPort();
		String rabbitHost = rabbit.getContainerIpAddress();
		String rabbitUser = rabbit.getAdminUsername();
		String rabbitPassword = rabbit.getAdminPassword();
		
		log.info("Rabbit Host [{}]",  rabbitHost);
		log.info("Rabbit Port [{}]",  rabbitPort);
		log.info("Rabbit Username [{}]",  rabbitUser);
		log.info("Rabbit password [{}]", rabbitPassword);
		
		log.info("Spring Host [{}]",  springPropHost);
		log.info("Spring Port [{}]",  springPropPort);
		log.info("Spring Username [{}]",  springPropUsername);
		log.info("Spring Password [{}]", springPropPassword);
		log.info("Spring Virtual Host [{}]", springPropVirtualHost);

		Assert.assertEquals(springPropUsername, rabbit.getAdminUsername());
		Assert.assertEquals(springPropPassword, rabbit.getAdminPassword());
		Assert.assertEquals(springPropHost, rabbit.getContainerIpAddress());
		Assert.assertEquals(springPropPort, rabbit.getAmqpPort());
	}
	
	@Test
	public void testSendRecv() throws UnsupportedOperationException, IOException, InterruptedException {
		
		String type1msg = "type1 test message";
		String type2msg = "type2 test message";
		sendService.sendMessage(QueueConfig.TYPE_ONE, type1msg);
		sendService.sendMessage(QueueConfig.TYPE_TWO, type2msg);

		TimeUnit.SECONDS.sleep(1);

		List<String> expected1 = Arrays.asList(type1msg);
		List<String> expected2 = Arrays.asList(type2msg);
		Assert.assertEquals(expected1, this.recvService.getType1Messages());
		Assert.assertEquals(expected2, this.recvService.getType2Messages());
	}

	@Test
	public void testCommandInsideContainer() throws Exception {
		String result = runCommandInDocker("type rabbitmqadmin");
		Assert.assertEquals("rabbitmqadmin is /usr/local/bin/rabbitmqadmin", result);
	}

	private String runCommandInDocker(String unixCommand) throws Exception {
		ExecResult res = rabbit.execInContainer(StandardCharsets.UTF_8, "/bin/bash", "-c", unixCommand);
		int exitCode = res.getExitCode();
		String stdErr = res.getStderr();
		String stdOut = res.getStdout();
		Assert.assertEquals(0, exitCode);
		log.info("cmd[{}] -> exit[{}] err[{}] out[{}]", unixCommand, exitCode, stdErr, stdOut);
		return stdOut.trim();
	}
	
	@Test
	public void testCheckQueuesInsideContainer() throws Exception {
		String result = runCommandInDocker("rabbitmqadmin list queues -f bash");
		Assert.assertEquals(QueueConfig.QUEUE_ONE+" "+QueueConfig.QUEUE_TWO, result);
	}
	
	@Test
	public void testExchangesInsideContainer() throws Exception {
		String result = runCommandInDocker("rabbitmqadmin list exchanges -f bash | xargs -n1 | grep -v amq");
		Assert.assertEquals(QueueConfig.TOPIC_ONE, result);
	}

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues values = TestPropertyValues.of(
                    "spring.rabbitmq.host=" + rabbit.getContainerIpAddress(),
                    "spring.rabbitmq.port=" + rabbit.getAmqpPort(),
                    "spring.rabbitmq.username=" + rabbit.getAdminUsername(),
                    "spring.rabbitmq.password=" + rabbit.getAdminPassword(),
                    "spring.rabbitmq.virtual-host=/"
            );
            values.applyTo(configurableApplicationContext.getEnvironment());
        }
    }
}
