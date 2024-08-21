package com.Ecommerce.project.repositories;

import com.Ecommerce.project.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByUserName(String userName);

    Boolean existsByUserName(String username);

    Boolean existsByUserEmail(String email);
}
