package edu.att4sd.controller.mqtt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MqttControllerConfig.class)
@Testcontainers
class MqttControllerTest {
	
	@Container
	public static final GenericContainer<?> mqttBroker = new GenericContainer<>(DockerImageName.parse("eclipse-mosquitto:2.0.7"))
							.withExposedPorts(1883)
							.withClasspathResourceMapping("mosquitto.conf", "/mosquitto/config/mosquitto.conf", BindMode.READ_ONLY)
							.waitingFor(Wait.forLogMessage(".*mosquitto version 2.0.7 running.*\\n", 1));

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		String address = "tcp://" + mqttBroker.getHost() + ":" + mqttBroker.getMappedPort(1883);
		registry.add("broker", () -> address);
	}

	@Value("${broker}")
	private String brokerAddress;
	
	@MockBean
	private MessageChannel mqttControllerOutputChannel;
	
	@Autowired
	private MqttController mqttReceiver;	
	
	private Logger logger = LoggerFactory.getLogger(MqttControllerTest.class);
	
	@Test
	void testMqttControllerConfiguration() {
		// Connection configurations
		assertThat(mqttReceiver.getConnectionInfo().isCleanSession()).isTrue();
		assertThat(mqttReceiver.getConnectionInfo().getConnectionTimeout()).isEqualTo(10);
		assertThat(mqttReceiver.getConnectionInfo().getKeepAliveInterval()).isEqualTo(90);
		assertThat(mqttReceiver.getConnectionInfo().isAutomaticReconnect()).isTrue();
		assertThat(Arrays.stream(mqttReceiver.getConnectionInfo().getServerURIs()).findFirst().orElse("ERROR")).isEqualTo(brokerAddress);
		// Mqtt configs
		assertThat(mqttReceiver.isAutoStartup()).isFalse();
		assertThat(Arrays.stream(mqttReceiver.getTopic())).isEmpty();
	}
	
	@Test
	void testConnectToTopicAndReceiveMessage() {
		String topic = "test/test";
		// Instruct mock
		when(mqttControllerOutputChannel.send(any(GenericMessage.class))).thenReturn(true);
		// 1) Connect receiver
		mqttReceiver.addTopic(topic, 0);
		mqttReceiver.start();
		// 2) Create publisher and publish a message
		DefaultMqttPahoClientFactory clientFactory = new DefaultMqttPahoClientFactory();
		String msgContent = "mytestmessage";
		try {
			IMqttClient mqttClient = clientFactory.getClientInstance(brokerAddress, "Producer");
			mqttClient.connect();
			MqttMessage msg = new MqttMessage(msgContent.getBytes());
			msg.setQos(2);
			// 3) send message
			mqttClient.publish(topic, msg);
			mqttClient.disconnect();
		} catch (MqttException e) {
			logger.error("Error while sending MQTT message. Cause: " + e.getMessage());
			e.printStackTrace();
		}
		logger.info("BROKER LOG:\n"+mqttBroker.getLogs());
		// 4) Verify message published
		verify(mqttControllerOutputChannel, times(1)).send(any(GenericMessage.class));
		mqttReceiver.stop();
		mqttReceiver.removeTopic(topic);
	}
}
