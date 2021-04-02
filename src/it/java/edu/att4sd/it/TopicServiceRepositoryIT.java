package edu.att4sd.it;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import edu.att4sd.model.TelemetryValue;
import edu.att4sd.model.Topic;
import edu.att4sd.repositories.TopicRepository;
import edu.att4sd.services.TopicService;

@ExtendWith(SpringExtension.class)
@DataMongoTest
@Import(TopicService.class)
public class TopicServiceRepositoryIT {
	
	@Autowired
	private TopicService topicService;
	
	@Autowired
	private TopicRepository topicRepository;
	
	@BeforeEach
	void cleanUpRepository() {
		topicRepository.deleteAll();
	}
	
	@Test
	void testServiceCanGetAllFromRepository() {
		List<Topic> saved = new ArrayList<>();
		saved.add(topicRepository.save(createTestTopic("test/path/1", "1.1")));
		saved.add(topicRepository.save(createTestTopic("test/path/2", "2.2")));
		
		List<Topic> found = topicService.getAllTopics();
		
		assertThat(found).containsAll(saved);
	}
	
	@Test
	void testServiceCanGetTopicByPathFromRepository() {
		Topic saved = topicRepository.save(createTestTopic("test/path/1", "1.1"));
		
		Topic found = topicService.getTopicByPath("test/path/1");
		
		assertThat(found).isEqualTo(saved);
	}
	
	
	@Test
	void testServiceCanInsertNewTopicIntoRepository() {
		Topic toInsert = createTestTopic("test/path/1", "1.1");
		
		Topic inserted = topicService.insertNewTopic(toInsert);
		
		assertThat(topicRepository.findAll()).contains(inserted);
	}
	
	@Test
	void testServiceCanRemoveTopicFromRepository() {
		Topic toRemove = topicRepository.save(createTestTopic("test/path/1", "1.1"));
		
		topicService.removeTopic(toRemove);
		
		assertThat(topicRepository.findAll()).doesNotContain(toRemove);
	}
	
	@Test
	void testServiceCanAddTelemetryValueIntoRepository() {
		Topic targetTopic = topicRepository.save(createTestTopic("test/path/1", "1.1"));
		TelemetryValue toAdd = new TelemetryValue(getTimestamp(), "2.3");
		
		topicService.addTelemetryValue("test/path/1", toAdd);
		
		targetTopic = topicRepository.findByPath("test/path/1").get();
		assertThat(targetTopic.getTelemetry()).contains(toAdd);
	}
	

	/* Utils */

	private Topic createTestTopic(String path, String... values) {
		Topic testTopic = new Topic(path, new ArrayList<>());
		Arrays.stream(values).forEach(value -> testTopic.getTelemetry()
													.add(new TelemetryValue(getTimestamp(), value)));
		return testTopic;
	}

	private Instant getTimestamp() {
		return Instant.now().truncatedTo(ChronoUnit.MILLIS);
	}

}
