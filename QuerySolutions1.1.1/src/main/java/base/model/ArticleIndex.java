package base.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
public class ArticleIndex {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;
	private String word;
	private String articleIds;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public List<Long> getArticleIds() {
		List<Long> articleIdsList = Stream.of(articleIds.split(","))
				  .map(String::trim)
				  .map(Long::parseLong)
				  .collect(Collectors.toList());

		return articleIdsList;
	}
	public void setArticleIds(String articleIds) {
		this.articleIds = articleIds;
	}
	
	
	
}
