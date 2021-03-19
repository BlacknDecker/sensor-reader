package edu.att4sd.controller.mqtt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import edu.att4sd.model.TelemetryValue;
import edu.att4sd.services.TopicService;


@ExtendWith(MockitoExtension.class)
class MqttMessageHandlerTest {
	
	@Mock
	private TopicService service;
		
	@InjectMocks
	private MqttMessageHandler mqttMsgHandler;
	

	@Test
	void testHandleMessage() {
		String topic = "/test/topic";
		String value = "TestMessage";
		Message<?> message = MessageBuilder.withPayload(value)
										   .setHeader(MqttHeaders.RECEIVED_TOPIC, topic)
										   .build();
		doNothing().when(service).addTelemetryValue(any(String.class), any(TelemetryValue.class));
		
		mqttMsgHandler.handleMessage(message);
		
		verify(service).addTelemetryValue(any(String.class), any(TelemetryValue.class));
	}
	
}
