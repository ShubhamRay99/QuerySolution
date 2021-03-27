package base.dao;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import base.model.Publisher;

public interface PublisherRepo extends JpaRepository<Publisher, Long>{

	Publisher findByUsername(String username);
	
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("update Publisher p set p.active =:active where p.id =:id")
	void changeStatePublisher(@Param("active") boolean active, Long id);

	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("update Publisher p set p.profilePic =:profilePic where p.username =:username")
	int UpdateProfilePic(@Param("profilePic") String profilePic, String username);
}
