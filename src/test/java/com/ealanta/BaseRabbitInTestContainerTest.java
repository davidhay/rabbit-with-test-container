package com.ealanta;

import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.BeforeClass;
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

	private static final Logger LOG = LoggerFactory.getLogger(BaseRabbitInTestContainerTest.class);

	public static final String CONFIG_FILE = "rabbit-config-for-test.json";
	public static final String CONFIG_FILE_PATH = "/" + CONFIG_FILE;
	
	private static final String RABBIT_BASE_IMAGE = "rabbitmq:3-management";
	public static final String RABBIT_DEFAULT_VIRTUAL_HOST = "/";
	public static final int RABBIT_PORT = 5672;
	public static final int RABBIT_MANAGEMENT_PORT = 15672;
	public static final String RABBIT_DEFAULT_USER = "guest";
	public static final String RABBIT_DEFAULT_PASS = "guest";

	@ClassRule
	public static GenericContainer<?> rabbit;
	
	static {
		rabbit = new GenericContainer<>(new ImageFromDockerfile()
				.withFileFromClasspath(CONFIG_FILE, CONFIG_FILE)
				.withDockerfileFromBuilder(builder -> builder
						.from(RABBIT_BASE_IMAGE)
						.add(CONFIG_FILE, CONFIG_FILE_PATH)
						.build())).withExposedPorts(RABBIT_PORT, RABBIT_MANAGEMENT_PORT);
	}

	@BeforeClass
	public static void importRabbitConfig() {
		try {
			String importCommand = String.format("rabbitmqadmin --vhost='%s' import %s", RABBIT_DEFAULT_VIRTUAL_HOST, CONFIG_FILE_PATH);
			String result = runCommandInDocker(importCommand);
			LOG.info("import result [{}]", result);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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

		int rabbitPort = rabbit.getMappedPort(RABBIT_PORT);
		String rabbitHost = rabbit.getContainerIpAddress();

		LOG.info("Rabbit Host [{}]", rabbitHost);
		LOG.info("Rabbit Port [{}]", rabbitPort);
		LOG.info("Rabbit Username [{}]", RABBIT_DEFAULT_USER);
		LOG.info("Rabbit password [{}]", RABBIT_DEFAULT_PASS);

		LOG.info("Spring Host [{}]", springPropHost);
		LOG.info("Spring Port [{}]", springPropPort);
		LOG.info("Spring Username [{}]", springPropUsername);
		LOG.info("Spring Password [{}]", springPropPassword);
		LOG.info("Spring Virtual Host [{}]", springPropVirtualHost);

		Assert.assertEquals(springPropUsername, RABBIT_DEFAULT_USER);
		Assert.assertEquals(springPropPassword, RABBIT_DEFAULT_PASS);
		Assert.assertEquals(springPropHost, rabbit.getContainerIpAddress());
		Assert.assertEquals(springPropPort, rabbit.getMappedPort(RABBIT_PORT));
	}

	protected static String runCommandInDocker(String unixCommand) throws Exception {
		ExecResult res = rabbit.execInContainer(StandardCharsets.UTF_8, "/bin/bash", "-c", unixCommand);
		int exitCode = res.getExitCode();
		String stdErr = res.getStderr().trim();
		String stdOut = res.getStdout().trim();
		Assert.assertEquals(0, exitCode);
		LOG.info("cmd[{}] -> exit[{}] err[{}] out[{}]", unixCommand, exitCode, stdErr, stdOut);
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
					"spring.rabbitmq.virtual-host="+ RABBIT_DEFAULT_VIRTUAL_HOST);
			values.applyTo(configurableApplicationContext.getEnvironment());
		}
	}
}
