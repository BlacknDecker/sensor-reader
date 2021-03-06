package edu.att4sd.services;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.att4sd.model.TelemetryValue;
import edu.att4sd.model.Topic;
import edu.att4sd.repositories.TopicRepository;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

	@Mock
	private TopicRepository repository;

	@InjectMocks
	private TopicService topicService;

	private Logger logger = LoggerFactory.getLogger(TopicServiceTest.class);

	@Test
	void testGetAllTopics() {
		Topic topic1 = createTestTopic("test/1", "12", "34");
		Topic topic2 = createTestTopic("test/2", "56", "78");
		when(repository.findAll()).thenReturn(asList(topic1, topic2));
		assertThat(topicService.getAllTopics()).containsExactly(topic1, topic2);
	}

	@Test
	void testGetTopicByPathWhenFound() {
		String testPath = "test/1";
		Topic topic = createTestTopic(testPath, "12", "34");
		when(repository.findByPath(testPath)).thenReturn(Optional.of(topic));
		assertThat(topicService.getTopicByPath(testPath)).isSameAs(topic);
	}

	@Test
	void testGetTopicByPathWhenNotFound() {
		when(repository.findByPath(anyString())).thenReturn(Optional.empty());
		assertThat(topicService.getTopicByPath("test")).isNull();
	}
	
	@Test
	void testInsertNewTopicWhenNotExists() {
		Topic toSave = spy(createTestTopic("test/1"));
		List<TelemetryValue> telemetry = toSave.getTelemetry();
		assertThat(telemetry).isEmpty();

		Topic saved = createTestTopic("test/1");
		when(repository.save(any(Topic.class))).thenReturn(saved);

		Topic inserted = topicService.insertNewTopic(toSave);

		assertThat(inserted).isSameAs(saved);
		verify(repository).save(toSave);
		assertThat(telemetry).isEmpty();
	}

	@Test
	void testInsertNewTopicShouldClearTelemetryOnInsert() {
		Topic toSave = spy(createTestTopic("test/1", "12", "34"));
		List<TelemetryValue> telemetry = toSave.getTelemetry();
		assertThat(telemetry).hasSize(2);
		
		Topic saved = createTestTopic("test/1");
		when(repository.save(any(Topic.class))).thenReturn(saved);
		
		Topic inserted = topicService.insertNewTopic(toSave);
		
		assertThat(inserted).isSameAs(saved);
		verify(repository).save(toSave);
		assertThat(telemetry).isEmpty();
	}

	@Test
	void testInsertNewTopicWhenAlreadyExistsShouldReturnExistentTopic() {
		String testPath = "test/1";
		Topic toSave = spy(createTestTopic(testPath));
		Topic saved = createTestTopic(testPath, "123", "456");
		when(repository.findByPath(testPath)).thenReturn(Optional.of(saved));
		
		Topic inserted = topicService.insertNewTopic(toSave);
		
		assertThat(inserted).isSameAs(saved);
		assertThat(inserted.getTelemetry()).hasSize(2);
	}
	
	
	

	/* Utils */

	private Topic createTestTopic(String path, String... values) {
		Topic testTopic = new Topic(path, new ArrayList<>());
		Arrays.stream(values).forEach(value -> testTopic.getTelemetry().add(new TelemetryValue(getTimestamp(), value)));
		return testTopic;
	}

	private Instant getTimestamp() {
		return Instant.now().truncatedTo(ChronoUnit.MILLIS);
	}

}
