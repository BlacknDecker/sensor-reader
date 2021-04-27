package edu.att4sd.controller.mqtt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
	
	@Captor
	private ArgumentCaptor<TelemetryValue> telemetryCaptor;
	
	@Captor
	private ArgumentCaptor<String> topicCaptor;

	@InjectMocks
	private MqttMessageHandler mqttMsgHandler;
	
	private static final String TOPIC_HEADER = MqttHeaders.RECEIVED_TOPIC; 
	private static final String TEST_TOPIC = "/test/topic";
	private static final String TEST_VALUE = "TestMessage";

	@Test
	void testHandleMessage() {
		Message<?> message = MessageBuilder.withPayload(TEST_VALUE)
										   .setHeader(TOPIC_HEADER, TEST_TOPIC)
										   .build();
		doNothing().when(service).addTelemetryValue(any(String.class), any(TelemetryValue.class));

		mqttMsgHandler.handleMessage(message);

		verify(service).addTelemetryValue(topicCaptor.capture(), telemetryCaptor.capture());
		assertThat(topicCaptor.getAllValues()).containsExactly(TEST_TOPIC);
		assertThat(telemetryCaptor.getAllValues()).hasSize(1);
		TelemetryValue captured = telemetryCaptor.getValue();
		assertThat(captured.getTimestamp()).isNotNull();
		assertThat(captured.getValue()).isEqualTo(TEST_VALUE);
	}

	@Test
	void testHandleMessageWithWrongHeader() {
		Message<?> message = MessageBuilder.withPayload(TEST_VALUE)
										   .setHeader("Wrong header", TEST_TOPIC)
										   .build();

		mqttMsgHandler.handleMessage(message);
		
		verifyNoInteractions(service);
	}

}
