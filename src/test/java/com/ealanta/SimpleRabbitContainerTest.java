package com.ealanta;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
//@ContextConfiguration(initializers = { SimpleRabbitContainerTest.Initializer.class })
public class SimpleRabbitContainerTest {

	private Integer rabbitPort;
	private String rabbitHost;
	private String rabbitUser;
	private String rabbitPassword;

	@Value("${spring.rabbitmq.username}")
	private String propUsername;
	
	@Value("${spring.rabbitmq.password}")
	private String propPassword;
	
	@Value("${spring.rabbitmq.host}")
	private String propHost;

	@Value("${spring.rabbitmq.port}")
	private Integer propPort;
	
	@Autowired
	private RecvService recvService;

	@Autowired
	private SendingService sendService;

	//@ClassRule
	//public static RabbitMQContainer rabbit = new RabbitMQContainer();

	@Before
	public void before() {
		//this.rabbitPort = rabbit.getAmqpPort();
		//this.rabbitHost = rabbit.getContainerIpAddress();
		//this.rabbitUser = rabbit.getAdminUsername();
		//this.rabbitPassword = rabbit.getAdminPassword();
	}

	@Test
	public void testSendRecv() throws UnsupportedOperationException, IOException, InterruptedException {
		System.out.printf("port [%d]%n", this.rabbitPort);
		System.out.printf("host [%s]%n", this.rabbitHost);
		System.out.printf("user [%s]%n", this.rabbitUser);
		System.out.printf("password [%s]%n", this.rabbitPassword);
		
		System.out.printf("SPRING port [%d]%n", this.propPort);
		System.out.printf("SPRING host [%s]%n", this.propHost);
		System.out.printf("SPRING user [%s]%n", this.propUsername);
		System.out.printf("SPRING password [%s]%n", this.propPassword);
		
		/*
		ExecResult res = rabbit.execInContainer(StandardCharsets.UTF_8, "/bin/bash", "-c", "type rabbitmqadmin");
		// ExecResult res = this.rabbit.execInContainer(StandardCharsets.UTF_8, "type
		// rabbitmqadmin");
		int exitCode = res.getExitCode();
		String stdErr = res.getStderr();
		String stdOut = res.getStdout();
		System.out.printf("fromDocker exit[%d] err[%s] out[%s]%n", exitCode, stdErr, stdOut);
		*/

		String type1msg = "type1 test message";
		String type2msg = "type2 test message";
		sendService.sendMessage("type1", type1msg);
		sendService.sendMessage("type2", type2msg);

		Thread.sleep(2000);

		List<String> expected1 = Arrays.asList(type1msg);
		List<String> expected2 = Arrays.asList(type2msg);
		Assert.assertEquals(expected1, this.recvService.getType1Messages());
		Assert.assertEquals(expected2, this.recvService.getType2Messages());
	}

	@After
	public void after() {
		//rabbit.stop();
	}

	/*
	static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			TestPropertyValues
					.of(	"spring.rabbitmq.password=" + rabbit.getAdminUsername(),
							"spring.rabbitmq.username=" + rabbit.getAdminPassword(),
							"spring.rabbitmq.port=" + rabbit.getAmqpPort())
					.applyTo(configurableApplicationContext.getEnvironment());
		}
	}
	*/
}
