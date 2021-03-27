package base.dao;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import base.model.ArticleVotes;

public interface ArticleVotesRepo extends JpaRepository<ArticleVotes, Long>{
	@Query("SELECT a FROM ArticleVotes a WHERE a.publisherEmail = ?1 and a.articleId = ?2 ")
	Optional<ArticleVotes> checkSign(String publisherEmail, Long articleId);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("update ArticleVotes a set a.sign =:sign where a.voteId =:voteId")
	void updatePublisherVote(@Param("sign") Integer sign, @Param("voteId") Long voteId);

	Optional<ArticleVotes> findByPublisherEmailAndArticleId(String publisherEmail, Long articleId);
	
	@Query("SELECT a FROM ArticleVotes a WHERE (a.publisherEmail = ?1 and a.sign = 1)")
	List<ArticleVotes> findByPublisherEmailOfLike(String publisherEmail);

	@Query("SELECT a FROM ArticleVotes a WHERE (a.publisherEmail = ?1 and a.sign = 0)")
	List<ArticleVotes> findByPublisherEmailOfdisLike(String publisherEmail);
}
