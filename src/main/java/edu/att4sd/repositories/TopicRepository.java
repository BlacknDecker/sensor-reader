package edu.att4sd.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import edu.att4sd.model.Topic;

public interface TopicRepository extends MongoRepository<Topic, String>{

}
