package edu.att4sd.repositories;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

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
	public void setUp() {
		MongoDatabase database = client.getDatabase(dbName);
		database.drop(); // Always start with a clean DB
		topicCollection = database.getCollection(topicCollectionName);
	}

	@Test
	public void testDBIsConnectedAndWorking() {
		Topic topic = new Topic("test", new ArrayList<>());
		Topic saved = repository.save(topic);
		Collection<Topic> topics = repository.findAll();
		assertThat(topics).containsExactly(saved);
	}
	
	@Test
	void testDocumentInsertionThroughClientWhenTopicHasEmptyTelemetry() {
		Topic toSave = new Topic("test", new ArrayList<>());
		// Insert documents into mongo db throgh MongoClient instead of using the repository
		topicCollection.insertOne(new Document()
				.append("path", toSave.getPath())
				.append("telemetry", toSave.getTelemetry()));
		Collection<Topic> topics = repository.findAll();
		assertThat(topics).singleElement().isEqualTo(toSave);
	}
	
	@Test
	void testDocumentInsertionThroughClientWhenTopicHasTelemetry() {
		TelemetryValue tValue = new TelemetryValue(Instant.now(), "1234");
		Topic toSave = new Topic("test", new ArrayList<>());
		toSave.getTelemetry().add(tValue);
		// Create Document
		Document topicDoc = new Document()
				.append("path", toSave.getPath())
				.append("telemetry", asList(
						new Document()
						.append("timestamp", tValue.getTimestamp())
						.append("value", tValue.getValue())));
		// Log (just for visual inspection)
		logger.info("TOPIC: " + toSave.toString());
		logger.info("TOPIC DOCUMENT: " + topicDoc.toString());
		// Test
		topicCollection.insertOne(topicDoc);
		Collection<Topic> topics = repository.findAll();
		assertThat(topics).singleElement().isEqualTo(toSave);
	}	

}
