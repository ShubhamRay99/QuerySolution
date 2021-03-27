package base.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import base.model.Article;

public interface ArticleRepo extends JpaRepository<Article, Long>{

	@Query("SELECT a FROM Article a WHERE a.publisherEmail = ?1")
	List<Article> findByPublisherEmail(String publisherEmail);
	
	@Query("SELECT u FROM Article u WHERE u.publisherEmail = ?1")
	Page<Article> findArticleOfUserOnPage(String publisherEmail, PageRequest pageRequest);

}
