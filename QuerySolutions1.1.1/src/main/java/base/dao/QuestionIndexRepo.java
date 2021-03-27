package base.dao;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import base.model.QuestionIndex;

public interface QuestionIndexRepo extends JpaRepository<QuestionIndex, Long>{

	Optional<QuestionIndex> findByWord(String word); 

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("update QuestionIndex q set q.questionIds =:questionIds where q.word =:word")
	void updateQuestionIds(@Param("questionIds")String questionIds,@Param("word") String word);

}
