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
		return repository.findByPath(path).orElse(null);
	}

	public Topic insertNewTopic(Topic topic) {
		topic.getTelemetry().clear();
		return repository.findByPath(topic.getPath())
				.orElse(repository.save(topic));
	}

	public void removeTopic(Topic toRemove) {
		repository.delete(toRemove);
	}

	public void addTelemetryValue(String topicPath, TelemetryValue newValue) {
		Topic toUpdate = repository.findByPath(topicPath)
							.orElseThrow(() -> new IllegalStateException("Topic not found!"));
		toUpdate.getTelemetry().add(newValue);
		repository.save(toUpdate);	
	}
	
}
