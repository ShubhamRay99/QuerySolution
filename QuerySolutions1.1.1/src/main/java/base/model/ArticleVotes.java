package base.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class ArticleVotes {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long voteId;
	private Long articleId;
	private String publisherEmail;
	private Integer sign;
	public Long getVoteId() {
		return voteId;
	}
	public void setVoteId(Long voteId) {
		this.voteId = voteId;
	}
	public Long getArticleId() {
		return articleId;
	}
	public void setArticleId(Long articleId) {
		this.articleId = articleId;
	}
	public String getPublisherEmail() {
		return publisherEmail;
	}
	public void setPublisherEmail(String publisherEmail) {
		this.publisherEmail = publisherEmail;
	}
	public Integer getSign() {
		return sign;
	}
	public void setSign(Integer sign) {
		this.sign = sign;
	}
	
	
}
