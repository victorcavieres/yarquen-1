package org.yarquen.topic;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Bean Topic representation 
 * @author maliq
 *
 */

@Document(collection="topics")
public class Topic {
	private String id;
	private String label;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	@Override
	public String toString() {
		return "Topic [id=" + id + ", label=" + label + "]";
	}

	
	
	
}
