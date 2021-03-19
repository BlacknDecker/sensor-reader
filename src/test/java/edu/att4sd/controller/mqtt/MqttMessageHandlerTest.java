package edu.att4sd.controller.mqtt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import edu.att4sd.model.TelemetryValue;
import edu.att4sd.services.TopicService;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {MqttMessageHandlerConfig.class, MqttReceiverConfig.class})
class MqttMessageHandlerTest {
	
	@MockBean
	private TopicService service;
	
	@Autowired
	private DefaultPahoMessageConverter mqttMessageConverter;
	
	@Autowired
	private MqttMessageHandler mqttMsgHandler;
	

	@Test
	void testHandleMessage() {
		String topic = "/test/topic";
		String value = "TestMessage";
		MqttMessage message = new MqttMessage(value.getBytes());
		Message<?> converted = mqttMessageConverter.toMessage(topic, message);
		doNothing().when(service).addTelemetryValue(any(String.class), any(TelemetryValue.class));
		
		mqttMsgHandler.handleMessage(converted);
		
		verify(service).addTelemetryValue(any(String.class), any(TelemetryValue.class));
	}
	
}
