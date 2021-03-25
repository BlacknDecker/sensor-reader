package edu.att4sd.services;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.att4sd.model.Topic;

@Service
public class TopicService {

	private static final String TEMPORARY_IMPLEMENTATION = "Temporary implementation";

	public List<Topic> getAllTopics() {
		throw new UnsupportedOperationException(TEMPORARY_IMPLEMENTATION);
	}
	
	public Topic insertNewTopic(Topic topic) {
		throw new UnsupportedOperationException(TEMPORARY_IMPLEMENTATION);
	}

	public Topic getTopicById(String string) {
		throw new UnsupportedOperationException(TEMPORARY_IMPLEMENTATION);
	}
}
