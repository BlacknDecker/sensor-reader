package edu.att4sd.repositories;

import java.util.List;

import org.springframework.stereotype.Repository;

import edu.att4sd.model.Topic;

@Repository
public class TopicRepository {

	private static final String TEMPORARY_IMPLEMENTATION = "Temporary implementation";
	
	public List<Topic> findAll() {
		throw new UnsupportedOperationException(TEMPORARY_IMPLEMENTATION);
	}

}
