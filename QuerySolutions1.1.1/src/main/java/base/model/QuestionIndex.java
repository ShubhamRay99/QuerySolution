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
public class QuestionIndex {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;
	private String word;
	private String questionIds;

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

	public List<Long> getQuestionIds() {
		List<Long> StringOfquestionIds = Stream.of(questionIds.split(",")).map(String::trim).map(Long::parseLong)
				.collect(Collectors.toList());

		return StringOfquestionIds;
	}

	public void setQuestionIds(String questionIds) {
		this.questionIds = questionIds;
	}

}
