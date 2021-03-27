package base.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import base.model.Authorities;

public interface AuthoritiesRepo extends JpaRepository<Authorities, String> {

}
