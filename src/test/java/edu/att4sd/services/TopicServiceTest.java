package edu.att4sd.services;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.att4sd.model.TelemetryValue;
import edu.att4sd.model.Topic;
import edu.att4sd.repositories.TopicRepository;


@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

	@Mock
	private TopicRepository repository;
	
	@InjectMocks
	private TopicService topicService;

	@Test
	void testGetAllTopics() {
		Topic topic1 = createTestTopic("test/1", "12", "34");
		Topic topic2 = createTestTopic("test/2", "56", "78");
		when(repository.findAll()).thenReturn(asList(topic1, topic2));
		assertThat(topicService.getAllTopics()).containsExactly(topic1, topic2);
	}
		
	
	/* Utils */ 
	
	private Topic createTestTopic(String path, String...values) {
		Topic testTopic = new Topic(path, new ArrayList<>());
		Arrays.stream(values)
				.forEach(value -> testTopic.getTelemetry()
											.add(new TelemetryValue(getTimestamp(), value)));
		return testTopic;
	}
	
	private Instant getTimestamp() {
		return Instant.now().truncatedTo(ChronoUnit.MILLIS);
	}

}
