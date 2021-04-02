package edu.att4sd.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import edu.att4sd.model.TelemetryValue;
import edu.att4sd.model.Topic;


@Import(MongoTestServerConfiguration.class)
@DataMongoTest
class TopicRepositoryTest {

	@Autowired
	private MongoClient client;

	@Autowired
	private TopicRepository repository;

	@Value("${spring.data.mongodb.database}")
	private String dbName;
	
	@Value("${spring.data.mongodb.topic_collection}")
	private String topicCollectionName;
	
	private MongoCollection<Document> topicCollection;
	
	private Logger logger = LoggerFactory.getLogger(TopicRepositoryTest.class);

	@BeforeEach
	void setUp() {
		// Always start with a clean DB
		MongoDatabase database = client.getDatabase(dbName);
		database.drop(); 										
		topicCollection = database.getCollection(topicCollectionName);
	}

	@Test
	void testRepositoryIsConnected() {
		Topic topic = createTestTopic("test");
		Topic saved = repository.save(topic);
		Collection<Topic> found = repository.findAll();
		assertThat(found).singleElement().isEqualTo(saved);
	}
	
	@Test
	void testDocumentInsertionThroughClientWhenTopicHasEmptyTelemetry() {
		Topic saved = createTestTopic("test");
		topicCollection.insertOne(topicToDocument(saved));
		Collection<Topic> found = repository.findAll();
		assertThat(found).singleElement().isEqualTo(saved);
	}
	
	@Test
	void testDocumentInsertionThroughClientWhenTopicHasTelemetry() {
		String tValue = "1234";
		Topic saved = createTestTopic("test", tValue);
		topicCollection.insertOne(topicToDocument(saved));
		List<Topic> found = repository.findAll();
		assertThat(found).singleElement().isEqualTo(saved);
		assertThat(found).toString().contains(tValue);
	}
	
	@Test
	void testFindByPathWhenTopicExists(){
		String topicPath = "test";
		Topic saved = createTestTopic(topicPath, "1234");		
		topicCollection.insertOne(topicToDocument(saved));
		topicCollection.insertOne(topicToDocument(createTestTopic("another", "5678")));
		Optional<Topic> found = repository.findByPath(topicPath);
		assertThat(found).isPresent()
							.containsInstanceOf(Topic.class)
							.contains(saved);
	}
	
	@Test
	void testFindByPathWhenTopicNotExists() {
		topicCollection.insertOne(topicToDocument(createTestTopic("another", "5678")));
		Optional<Topic> notFound = repository.findByPath("test");
		assertThat(notFound).isEmpty();
	}
	
	@Test
	void testFindByPathWhenDBisEmpty() {
		assertThat(topicCollection.find()).isEmpty();
		Optional<Topic> notFound = repository.findByPath("test");
		assertThat(notFound).isEmpty();
	}

	
	/* Utilities */
	
	private Topic createTestTopic(String path, String...values) {
		Topic testTopic = new Topic(path, new ArrayList<>());
		Arrays.stream(values)
				.forEach(value -> testTopic.getTelemetry().add(new TelemetryValue(getTimestamp(), value)));
		return testTopic;
	}
	
	private Document topicToDocument(Topic topic) {
		Document document = new Document();
		document.append("path",	topic.getPath());
		List<Document> telemetry = topic.getTelemetry()
											.stream()
											.map(tValue -> telemetryValueToDocument(tValue))
											.collect(Collectors.toList());
		document.append("telemetry", telemetry);
		return document;
	}
	
	private Document telemetryValueToDocument(TelemetryValue telemetry) {
		return new Document()
				.append("timestamp", telemetry.getTimestamp())
				.append("value", telemetry.getValue());
	}
	
	private Instant getTimestamp() {
		return Instant.now().truncatedTo(ChronoUnit.MILLIS);
	}
	

}
