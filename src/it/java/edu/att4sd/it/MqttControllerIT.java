package edu.att4sd.it;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import edu.att4sd.controller.mqtt.MqttReceiver;
import edu.att4sd.it.utilities.MqttChannelInterceptor;
import edu.att4sd.model.TelemetryValue;
import edu.att4sd.model.Topic;
import edu.att4sd.repositories.TopicRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class MqttControllerIT {
	
	@Autowired
	private TopicRepository topicRepository;
	
	@Autowired
	private MqttReceiver mqttReceiver;
		
	@Autowired
	@Qualifier("mqttReceiverOutputChannel")
	private DirectChannel mqttChannel;
	
	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {	
		String address = "tcp://localhost:1883";
		registry.add("broker", () -> address);						// Override Broker address
	}

	@Value("${broker}")
	private String brokerAddress;
	
	private Logger logger = LoggerFactory.getLogger(MqttControllerIT.class);
	private static final MqttChannelInterceptor mqttChannelInterceptor = new MqttChannelInterceptor();
	private static final String TOPIC_HEADER = MqttHeaders.RECEIVED_TOPIC; 
	private static final String TEST_TOPIC = "/test/topic";
	private static final String TEST_VALUE = "1.1";
		
	
	
	@BeforeEach
	void resetParameters() {
		// Interceptor
		if(mqttChannel.getInterceptors().size() == 0) {
			logger.info("Adding interceptor...");
			mqttChannel.addInterceptor(mqttChannelInterceptor);			
		}
		mqttChannelInterceptor.resetCount();
		// Repository
		topicRepository.deleteAll();
		topicRepository.save(new Topic(TEST_TOPIC, new ArrayList<>()));
	}
	
	
	@Test
	void testMessageHandlerSavesReceivedTelemetry() {
		// Create message
		Message<?> message = MessageBuilder.withPayload(TEST_VALUE)
				   				.setHeader(TOPIC_HEADER, TEST_TOPIC)
				   				.build();
		// Send message
		mqttChannel.send(message);
		// Verify
		await().atMost(3, SECONDS).until(topicTelemetrySize(TEST_TOPIC), equalTo(1));
		List<TelemetryValue> saved = topicRepository.findByPath(TEST_TOPIC).get().getTelemetry();
		assertThat(saved).first().hasFieldOrPropertyWithValue("value", TEST_VALUE);
		assertThat(mqttChannelInterceptor.sendCount.get()).isEqualTo(1);
	}
	
	
	@Test
	void testMqttControllerSavesTelemetry() {
		connectReceiver();
		
		sendTestTelemetry(TEST_TOPIC, TEST_VALUE);
		
		await().atMost(10, SECONDS).until(topicTelemetrySize(TEST_TOPIC), not(equalTo(0)));
		disconnectReceiver();
		// Verify
		List<TelemetryValue> saved = topicRepository.findByPath(TEST_TOPIC).get().getTelemetry();
		assertThat(saved).hasSize(1);
		assertThat(saved).first().hasFieldOrPropertyWithValue("value", TEST_VALUE);
	}
	
	@Test
	void testMqttControllerHandlesMultipleMessageOnSameTopic() {
		connectReceiver();
		int telemetry_size = 10;
		
		IntStream.iterate(0, i -> i+1)
				 .limit(telemetry_size)
				 .boxed()
				 .forEach(n -> sendTestTelemetry(TEST_TOPIC, "1."+n));
				
		await().atMost(10, SECONDS).until(topicTelemetrySize(TEST_TOPIC), equalTo(telemetry_size));
		disconnectReceiver();		
	}
	

	/* Utilities */
	
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
	
	private Callable<Integer> topicTelemetrySize(String path){
		return () -> topicRepository.findByPath(path).get().getTelemetry().size();
	}
		
	private void connectReceiver() {
		mqttReceiver.addTopic(TEST_TOPIC, 0);
		assertThat(mqttReceiver.getTopic()).hasSize(1);
		assertThat(Arrays.stream(mqttReceiver.getTopic())).first().isEqualTo(TEST_TOPIC);
		mqttReceiver.start();
	}
		
	private void disconnectReceiver() {
		mqttReceiver.stop();
		mqttReceiver.removeTopic(TEST_TOPIC);
		assertThat(mqttReceiver.getTopic()).isEmpty();
	}
	
}

