package edu.att4sd.repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import edu.att4sd.model.Topic;

public interface TopicRepository extends MongoRepository<Topic, String> {

	public Optional<Topic> findByPath(String path);

}
