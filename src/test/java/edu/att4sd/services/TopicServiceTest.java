package edu.att4sd.services;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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

	private static final String TOPIC_ID = "qwerty";
	private static final String TOPIC_PATH = "test/1";
	private static final String VALUE1 = "12";
	private static final String VALUE2 = "34";

	private Logger logger = LoggerFactory.getLogger(TopicServiceTest.class);

	@Test
	void testGetAllTopicsWhenDbIsEmpty() {
		when(repository.findAll()).thenReturn(new ArrayList<>());
		assertThat(topicService.getAllTopics()).isEmpty();
	}

	@Test
	void testGetAllTopics() {
		Topic topic1 = createTestTopic(TOPIC_PATH, VALUE1, VALUE2);
		Topic topic2 = createTestTopic("test/2", "56", "78");
		when(repository.findAll()).thenReturn(asList(topic1, topic2));
		assertThat(topicService.getAllTopics()).containsExactly(topic1, topic2);
	}

	@Test
	void testGetTopicByPathWhenFound() {
		Topic topic = createTestTopic(TOPIC_PATH, VALUE1, VALUE2);
		when(repository.findByPath(TOPIC_PATH)).thenReturn(Optional.of(topic));
		assertThat(topicService.getTopicByPath(TOPIC_PATH)).isSameAs(topic);
	}

	@Test
	void testGetTopicByPathWhenNotFoundShouldThrow() {
		when(repository.findByPath(anyString())).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> topicService.getTopicByPath("test"));
	}

	@Test
	void testGetTopicByIdWhenFound() {
		Topic topic = createTestTopic(TOPIC_PATH, VALUE1, VALUE2);
		when(repository.findById(TOPIC_ID)).thenReturn(Optional.of(topic));
		assertThat(topicService.getTopicById(TOPIC_ID)).isSameAs(topic);
	}

	@Test
	void testGetTopicByIdWhenNotFoundShouldThrow() {
		when(repository.findById(anyString())).thenReturn(Optional.empty());
		assertThrows(IllegalArgumentException.class, () -> topicService.getTopicById("test"));
	}

	@Test
	void testInsertNewTopicWhenNotExists() {
		Topic toSave = spy(createTestTopic(TOPIC_PATH));
		assertThat(toSave.getTelemetry()).isEmpty();

		Topic saved = createTestTopic(TOPIC_PATH);
		when(repository.save(any(Topic.class))).thenReturn(saved);

		Topic inserted = topicService.insertNewTopic(toSave);

		assertThat(inserted).isSameAs(saved);
		verify(repository).save(toSave);
		assertThat(toSave.getTelemetry()).isEmpty();
	}

	@Test
	void testInsertNewTopicShouldClearTelemetryOnInsert() {
		Topic toSave = spy(createTestTopic(TOPIC_PATH, VALUE1, VALUE2));
		assertThat(toSave.getTelemetry()).hasSize(2);

		Topic saved = createTestTopic(TOPIC_PATH);
		when(repository.save(any(Topic.class))).thenReturn(saved);

		Topic inserted = topicService.insertNewTopic(toSave);

		assertThat(inserted).isSameAs(saved);
		verify(repository).save(toSave);
		assertThat(toSave.getTelemetry()).isEmpty();
	}

	@Test
	void testInsertNewTopicWhenAlreadyExistsShouldReturnExistentTopicAndIgnoreDuplicate() {
		Topic duplicate = createTestTopic(TOPIC_PATH);
		Topic saved = createTestTopic(TOPIC_PATH, VALUE1, VALUE2);
		when(repository.findByPath(TOPIC_PATH)).thenReturn(Optional.of(saved));

		Topic inserted = topicService.insertNewTopic(duplicate);

		verify(repository, times(0)).save(any(Topic.class));
		assertThat(inserted).isSameAs(saved);
		assertThat(inserted.getTelemetry()).hasSize(2);
	}

	@Test
	void testRemoveTopicWhenTopicExists() {
		Topic toRemove = createTestTopic(TOPIC_PATH, VALUE1);
		doNothing().when(repository).delete(toRemove);

		boolean result = topicService.removeTopic(toRemove);

		verify(repository).delete(toRemove);
		assertThat(result).isTrue();
	}

	@Test
	void testRemoveTopicWhenTopicNotExists() {
		Topic notToRemove = createTestTopic(TOPIC_PATH, VALUE1);
		doThrow(new IllegalArgumentException()).when(repository).delete(notToRemove);
		
		boolean result = topicService.removeTopic(notToRemove);
		
		assertThat(result).isFalse();
	}

	@Test
	void testRemoveTopicByIdWhenTopicExists() {
		doNothing().when(repository).deleteById(any(String.class));

		boolean result = topicService.removeTopicById(TOPIC_ID);

		verify(repository).deleteById(TOPIC_ID);
		assertThat(result).isTrue();
	}

	@Test
	void testRemoveTopicByIdWhenTopicNotExists() {
		doThrow(new IllegalArgumentException()).when(repository).deleteById(TOPIC_ID);
		
		boolean result = topicService.removeTopicById(TOPIC_ID);
		
		assertThat(result).isFalse();
	}

	@Test
	void testAddTelemetryValueWhenTopicFound() {
		TelemetryValue newValue = new TelemetryValue(getTimestamp(), VALUE1);
		Topic emptyTopic = createTestTopic(TOPIC_PATH);
		Topic updated = createTestTopic(TOPIC_PATH);
		updated.getTelemetry().add(newValue);
		when(repository.findByPath(TOPIC_PATH)).thenReturn(Optional.of(emptyTopic));
		when(repository.save(any(Topic.class))).thenReturn(updated);

		topicService.addTelemetryValue(TOPIC_PATH, newValue);

		InOrder inOrder = Mockito.inOrder(repository);
		inOrder.verify(repository).findByPath(TOPIC_PATH);
		inOrder.verify(repository).save(updated);
	}

	@Test
	void testAddTelemetryValueWhenTopicNotFoundShouldThrow() {
		TelemetryValue newValue = new TelemetryValue(getTimestamp(), VALUE1);
		when(repository.findByPath(TOPIC_PATH)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class, () -> topicService.addTelemetryValue(TOPIC_PATH, newValue));
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
