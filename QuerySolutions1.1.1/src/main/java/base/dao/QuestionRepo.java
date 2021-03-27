package base.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import base.model.Question;

public interface QuestionRepo extends JpaRepository<Question, Long>{

	@Query("SELECT u FROM Question u WHERE u.publisherEmail = ?1")
	List<Question> findContentsOfUser(String publisherEmail);

	@Query("SELECT u FROM Question u WHERE u.publisherEmail = ?1")
	Page<Question> findContentsOfUserOnPage(String publisherEmail, PageRequest pageRequest);

}
