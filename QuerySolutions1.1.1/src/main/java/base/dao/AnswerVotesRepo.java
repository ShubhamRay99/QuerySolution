package base.dao;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import base.model.AnswerVotes;

public interface AnswerVotesRepo extends JpaRepository<AnswerVotes, Long>{

	@Query("SELECT a FROM AnswerVotes a WHERE (a.publisherEmail = ?1 and a.answerId = ?2 ) and a.questionId = ?3")
	Optional<AnswerVotes> checkSign(String publisherEmail, Long answerId, Long questionId);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("update AnswerVotes a set a.sign =:sign where a.voteId =:voteId")
	void updatePublisherVote(@Param("sign") Integer sign, @Param("voteId") Long voteId);

	Optional<AnswerVotes> findByPublisherEmailAndAnswerId(String publisherEmail, Long answerId);

	List<AnswerVotes> findAllByAnswerId(Long answerId);
	
	@Query("SELECT a FROM AnswerVotes a WHERE (a.publisherEmail = ?1 and a.sign = 1)")
	List<AnswerVotes> findByPublisherEmailOfLike(String publisherEmail);

	@Query("SELECT a FROM AnswerVotes a WHERE (a.publisherEmail = ?1 and a.sign = 0)")
	List<AnswerVotes> findByPublisherEmailOfdisLike(String publisherEmail);
}
