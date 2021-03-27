package base.model;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table
public class Question {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long qId;
	private String topic;
	@Lob
	private String description;
	private String tags;
	private String publisherEmail;
	@CreationTimestamp
	private Date createdOn;
	

	public Question() {
		
	}
	public Question(Long qId, String topic, String description, String tags, String publisherEmail, Date createdOn) {
		this.qId = qId;
		this.topic = topic;
		this.description = description;
		this.tags = tags;
		this.publisherEmail = publisherEmail;
		this.createdOn = createdOn;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Long getqId() {
		return qId;
	}

	public void setqId(Long qId) {
		this.qId = qId;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getPublisherEmail() {
		return publisherEmail;
	}

	public void setPublisherEmail(String publisherEmail) {
		this.publisherEmail = publisherEmail;
	}

	@Override
	public String toString() {
		return "Question [qId=" + qId + ", topic=" + topic + ", description=" + description + ", tags=" + tags
				+ ", publisherEmail=" + publisherEmail + "]";
	}

}
