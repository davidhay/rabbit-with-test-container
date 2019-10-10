package com.ealanta;

import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RabbitWithTestContainerApplication.class)
@ContextConfiguration(initializers = { BaseRabbitInTestContainerTest.Initializer.class })

@ActiveProfiles("test")
public abstract class BaseRabbitInTestContainerTest {

	private static final Logger log = LoggerFactory.getLogger(BaseRabbitInTestContainerTest.class);

	public static final String CONFIG_FILE = "rabbit-config-for-test.json";
	public static final String CONFIG_FILE_PATH = "/" + CONFIG_FILE;
	
	public static final int RABBIT_PORT = 5672;
	public static final int RABBIT_MANAGEMENT_PORT = 15672;
	public static final String RABBIT_DEFAULT_USER="guest";
	public static final String RABBIT_DEFAULT_PASS="guest";

	@ClassRule
	public static GenericContainer rabbit;
	
	static {
		rabbit = new GenericContainer(new ImageFromDockerfile()
				.withFileFromClasspath(CONFIG_FILE, CONFIG_FILE)
				.withDockerfileFromBuilder(builder -> builder
						.from("rabbitmq:3-management")
						.add(CONFIG_FILE, CONFIG_FILE_PATH)
						.build())).withExposedPorts(RABBIT_PORT, RABBIT_MANAGEMENT_PORT);
	}

	// helps check that Spring is using overridden property value from rabbit test
	// container
	@Value("${spring.rabbitmq.username}")
	private String springPropUsername;

	// helps check that Spring is using overridden property value from rabbit test
	// container
	@Value("${spring.rabbitmq.password}")
	private String springPropPassword;

	// helps check that Spring is using overridden property value from rabbit test
	// container
	@Value("${spring.rabbitmq.host}")
	private String springPropHost;

	// helps check that Spring is using overridden property value from rabbit test
	// container
	@Value("${spring.rabbitmq.port}")
	private Integer springPropPort;

	// helps check that Spring is using overridden property value from rabbit test
	// container
	@Value("${spring.rabbitmq.virtual-host}")
	private String springPropVirtualHost;

	@Test
	public void baseTestCheckSpringRabbitPropertiesFromTestContainer() {

		int rabbitPort = rabbit.getMappedPort(5672);
		String rabbitHost = rabbit.getContainerIpAddress();
		String rabbitUser = "rabbit";
		String rabbitPassword = "bunny";

		log.info("Rabbit Host [{}]", rabbitHost);
		log.info("Rabbit Port [{}]", rabbitPort);
		log.info("Rabbit Username [{}]", rabbitUser);
		log.info("Rabbit password [{}]", rabbitPassword);
		// odd but RabbitMQContainer does not (seem to ) have method to get virtual host

		log.info("Spring Host [{}]", springPropHost);
		log.info("Spring Port [{}]", springPropPort);
		log.info("Spring Username [{}]", springPropUsername);
		log.info("Spring Password [{}]", springPropPassword);
		log.info("Spring Virtual Host [{}]", springPropVirtualHost);

		Assert.assertEquals(springPropUsername, RABBIT_DEFAULT_PASS);
		Assert.assertEquals(springPropPassword, RABBIT_DEFAULT_USER);
		Assert.assertEquals(springPropHost, rabbit.getContainerIpAddress());
		Assert.assertEquals(springPropPort, rabbit.getMappedPort(5672));
	}

	protected static String runCommandInDocker(String unixCommand) throws Exception {
		ExecResult res = rabbit.execInContainer(StandardCharsets.UTF_8, "/bin/bash", "-c", unixCommand);
		int exitCode = res.getExitCode();
		String stdErr = res.getStderr().trim();
		String stdOut = res.getStdout().trim();
		Assert.assertEquals(0, exitCode);
		log.info("cmd[{}] -> exit[{}] err[{}] out[{}]", unixCommand, exitCode, stdErr, stdOut);
		return stdOut;
	}

	@Test
	public void baseTestCheckCommandInTestContainer() throws Exception {
		String result = runCommandInDocker("type rabbitmqadmin");
		Assert.assertEquals("rabbitmqadmin is /usr/local/bin/rabbitmqadmin", result);
	}

	public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		@Override
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			TestPropertyValues values = TestPropertyValues.of(
					"spring.rabbitmq.host=" + rabbit.getContainerIpAddress(),
					"spring.rabbitmq.port=" + rabbit.getMappedPort(RABBIT_PORT),
					"spring.rabbitmq.username=" + RABBIT_DEFAULT_USER,
					"spring.rabbitmq.password=" + RABBIT_DEFAULT_PASS, 
					"spring.rabbitmq.virtual-host=/");
			values.applyTo(configurableApplicationContext.getEnvironment());
		}
	}
}
