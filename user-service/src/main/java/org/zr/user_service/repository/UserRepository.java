package org.zr.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zr.user_service.domain.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
