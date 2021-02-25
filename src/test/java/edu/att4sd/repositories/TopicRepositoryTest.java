package edu.att4sd.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import edu.att4sd.model.Topic;

@ExtendWith(SpringExtension.class)
@Testcontainers
@DataMongoTest
class TopicRepositoryTest {

	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2").withExposedPorts(27017);

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
	}

	@Autowired
	private MongoClient client;

	@Autowired
	private TopicRepository repository;

	@Value("${spring.data.mongodb.database}")
	private String dbName;

	private Logger logger = LoggerFactory.getLogger(TopicRepositoryTest.class);

	@BeforeEach
	public void setUp() {
		MongoDatabase database = client.getDatabase(dbName);
		database.drop(); // Always start with a clean DB
	}

	@Test
	public void testDBIsConnectedAndWorking() {
		Topic topic = new Topic("test", new ArrayList<>());
		Topic saved = repository.save(topic);
		Collection<Topic> topics = repository.findAll();
		assertThat(topics).containsExactly(saved);
	}

}
