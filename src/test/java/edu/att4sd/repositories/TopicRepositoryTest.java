package edu.att4sd.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
		Collection<Topic> topics = repository.findAll();
		assertThat(topics).singleElement().isEqualTo(saved);
	}
	
	@Test
	void testDocumentInsertionThroughClientWhenTopicHasEmptyTelemetry() {
		Topic toSave = createTestTopic("test");
		// Insert documents into mongo db throgh MongoClient instead of using the repository
		topicCollection.insertOne(topicToDocument(toSave));
		Collection<Topic> topics = repository.findAll();
		assertThat(topics).singleElement().isEqualTo(toSave);
	}
	
	@Test
	void testDocumentInsertionThroughClientWhenTopicHasTelemetry() {
		String tValue = "1234";
		Topic toSave = createTestTopic("test", tValue);
		logger.info("TOPIC: " + toSave.toString());
		Document topicDoc = topicToDocument(toSave);
		logger.info("TOPIC DOCUMENT: " + topicDoc.toString());
		topicCollection.insertOne(topicDoc);
		List<Topic> topics = repository.findAll();
		assertThat(topics).singleElement().isEqualTo(toSave);
		Topic saved = topics.get(0);
		assertThat(saved.getTelemetry().get(0).getValue()).isEqualTo(tValue);
	}	
	
	private Topic createTestTopic(String path, String...values) {
		Topic testTopic = new Topic(path, new ArrayList<>());
		Arrays.stream(values)
				.forEach(value -> testTopic.getTelemetry().add(new TelemetryValue(Instant.now(), value)));
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
	

}
