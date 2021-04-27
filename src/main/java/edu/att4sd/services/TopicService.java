package edu.att4sd.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.att4sd.model.TelemetryValue;
import edu.att4sd.model.Topic;
import edu.att4sd.repositories.TopicRepository;

@Service
public class TopicService {
	
	@Autowired
	private TopicRepository repository;

	public List<Topic> getAllTopics() {
		return repository.findAll();
	}
	
	public Topic getTopicByPath(String path) {
		return repository.findByPath(path)
				.orElseThrow(() -> new IllegalArgumentException("Topic not found!"));
	}
	
	public Topic getTopicById(String id) {
		return repository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Topic not found!"));
	}
	
	public Topic insertNewTopic(Topic topic) {
		topic.getTelemetry().clear();
		return repository.findByPath(topic.getPath())
				.orElse(repository.save(topic));
	}

	public boolean removeTopic(Topic toRemove) {
		try {
			repository.delete(toRemove);			
		}catch (IllegalArgumentException e) {
			return false;
		}
		return true;
	}
	
	public boolean removeTopicById(String id) {
		try {
			repository.deleteById(id);			
		}catch (IllegalArgumentException e) {
			return false;
		}
		return true;
	}

	public void addTelemetryValue(String topicPath, TelemetryValue newValue) {
		Topic toUpdate = repository.findByPath(topicPath)
							.orElseThrow(() -> new IllegalArgumentException("Topic not found!"));
		toUpdate.getTelemetry().add(newValue);
		repository.save(toUpdate);	
	}
	
}
