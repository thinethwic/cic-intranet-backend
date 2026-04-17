package com.intranet.cic.repositories;

import com.intranet.cic.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepositories extends JpaRepository<User, Long> {
}
