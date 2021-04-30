package edu.att4sd.controller.mqtt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.support.MqttMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {"mqtt.receiver.autostartup=false"})
@ContextConfiguration(classes = MqttReceiverConfig.class)
@Testcontainers
class MqttReceiverTest {
	
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
	
	@MockBean(name="mqttReceiverOutputChannel")
	private MessageChannel mqttReceiverOutputChannel;
	
	@SpyBean
	private MqttMessageConverter mqttMessageConverter;
	
	@Autowired
	private MqttReceiver mqttReceiver;	
	
	private Logger logger = LoggerFactory.getLogger(MqttReceiverTest.class);
	
	private static final String TEST_TOPIC_PATH = "/test/path";
	private static final String TEST_TELEMETRYVALUE = "1.1";
		
	@BeforeEach
	void setup() {
		if(mqttReceiver.getTopic().length > 1) {
			mqttReceiver.removeTopic(TEST_TOPIC_PATH);
		}
		assertThat(mqttReceiver.getTopic()).containsExactly(mqttReceiver.getDefaultTopic());
	}
	
	@Test
	void testMqttReceiverConfiguration() {
		// Connection configurations
		assertThat(mqttReceiver.getConnectionInfo().isCleanSession()).isTrue();
		assertThat(mqttReceiver.getConnectionInfo().getConnectionTimeout()).isEqualTo(10);
		assertThat(mqttReceiver.getConnectionInfo().getKeepAliveInterval()).isEqualTo(90);
		assertThat(mqttReceiver.getConnectionInfo().isAutomaticReconnect()).isTrue();
		assertThat(Arrays.stream(mqttReceiver.getConnectionInfo().getServerURIs()).findFirst().orElse("ERROR")).isEqualTo(brokerAddress);
		// Mqtt configs
		assertThat(mqttReceiver.isAutoStartup()).isFalse();  //Test only
	}
	
	@Test
	void testAddTopicIgnoreDuplicatesWhenTopicIsNew() {
		mqttReceiver.addTopicIgnoreDuplicates(TEST_TOPIC_PATH, 0);
		
		assertThat(Arrays.stream(mqttReceiver.getTopic()))
			.containsExactly(mqttReceiver.getDefaultTopic(), 
							 TEST_TOPIC_PATH);
	}
	
	@Test
	void testAddTopicIgnoreDuplicatesWhenTopicIsAlreadySubscribed() {
		mqttReceiver.addTopicIgnoreDuplicates(TEST_TOPIC_PATH, 0);
		
		mqttReceiver.addTopicIgnoreDuplicates(TEST_TOPIC_PATH, 2);
		
		assertThat(Arrays.stream(mqttReceiver.getTopic()))
			.containsExactly(mqttReceiver.getDefaultTopic(), 
							 TEST_TOPIC_PATH);
		// Checks that the quality of service has been updated
		assertThat(mqttReceiver.getQos()[1]).isEqualTo(2); 				
	}
		
	@Test
	void testMqttReceiverWhenReceiveShouldForwardOnOutputChannel() {
		when(mqttReceiverOutputChannel.send(any(GenericMessage.class))).thenReturn(true);
		mqttReceiver.addTopic(TEST_TOPIC_PATH, 2);
		assertThat(Arrays.stream(mqttReceiver.getTopic()))
			.containsExactly(mqttReceiver.getDefaultTopic(), 
							 TEST_TOPIC_PATH);
		
		mqttReceiver.start();
		sendTestTelemetry(TEST_TOPIC_PATH, TEST_TELEMETRYVALUE);
		mqttReceiver.stop();
		mqttReceiver.removeTopic(TEST_TOPIC_PATH);
		
		logger.info("BROKER LOG:\n"+mqttBroker.getLogs());
		verify(mqttReceiverOutputChannel, times(1)).send(any(GenericMessage.class));
		verify(mqttMessageConverter).toMessageBuilder(eq(TEST_TOPIC_PATH), any(MqttMessage.class));
	}
	
	private void sendTestTelemetry(String topic, String value) {
		DefaultMqttPahoClientFactory clientFactory = new DefaultMqttPahoClientFactory();
        MqttMessage message = new MqttMessage(value.getBytes());
		message.setQos(2);	// To send just one message
		try {
			IMqttClient mqttClient = clientFactory.getClientInstance(brokerAddress, "Producer");
			mqttClient.connect();
			mqttClient.publish(topic, message);		
			mqttClient.disconnect();
		} catch (MqttException e) {
			logger.error("Error while sending MQTT message. Cause: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
