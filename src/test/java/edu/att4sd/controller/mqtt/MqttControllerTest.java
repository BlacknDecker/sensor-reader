package edu.att4sd.controller.mqtt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MqttControllerConfig.class)
class MqttControllerTest {

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		String address = "tcp://localhost";
		registry.add("broker", () -> address);
	}

	@Value("${broker}")
	private String brokerAddress;
	
	@Autowired
	private MqttController mqttReceiver;
		
	@MockBean
	private DirectChannel mqttControllerOutputChannel;
	
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
	
}
