package edu.att4sd.repositories;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;


@TestConfiguration
public class MongoTestServerConfiguration {

	@Bean(destroyMethod = "shutdown")
	public MongoServer mongoServer() {
		MongoServer mongoServer = new MongoServer(new MemoryBackend());
		mongoServer.bind();
		return mongoServer;
	}

	@Bean(destroyMethod = "close")
	public MongoClient mongoClient(MongoServer mongoServer) {
		return MongoClients.create("mongodb://" + mongoServer.getLocalAddress().getHostName() + ":"
				+ mongoServer.getLocalAddress().getPort());
	}
}
