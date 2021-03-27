package base.dao;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import base.model.ArticleIndex;

public interface ArticleIndexRepo extends JpaRepository<ArticleIndex, Long>{

	Optional<ArticleIndex> findByWord(String word); 
	
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("update ArticleIndex a set a.articleIds =:articleIds where a.word =:word")
	void updateArticleIds(@Param("articleIds")String articleIds,@Param("word") String word);

}
