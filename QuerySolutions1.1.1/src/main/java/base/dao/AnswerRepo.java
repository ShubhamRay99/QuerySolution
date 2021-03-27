package base.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import base.model.Answers;

public interface AnswerRepo extends JpaRepository<Answers, Long>{

	@Query("SELECT a FROM Answers a WHERE a.questionId = ?1")
	List<Answers> findByQuestionId(Long questionId);

	@Query("SELECT a FROM Answers a WHERE a.publisherEmail = ?1")
	List<Answers> findByPublisherEmail(String publisherEmail);

}
