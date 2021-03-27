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
public class Article {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long articleId;
	private String topic;
	@Lob
	private String content;
	private String tags;
	private String publisherEmail;
	@CreationTimestamp
	private Date createdOn;
	
	
	
	public Article() {}
	
	public Article(Long articleId, String topic, String content, String tags, String publisherEmail, Date createdOn) {
		this.articleId = articleId;
		this.topic = topic;
		this.content = content;
		this.tags = tags;
		this.publisherEmail = publisherEmail;
		this.createdOn = createdOn;
	}
	public Long getArticleId() {
		return articleId;
	}
	public void setArticleId(Long articleId) {
		this.articleId = articleId;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
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
	public Date getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}
	
	
}
