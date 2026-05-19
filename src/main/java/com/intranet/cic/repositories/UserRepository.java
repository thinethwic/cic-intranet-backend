package com.intranet.cic.repositories;

import com.intranet.cic.entities.User;
//import com.intranet.cic.entities.types.Segment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

//import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

// Custom Email Query

    //    @Query("""
//    SELECT u FROM User u
//    WHERE u.role = 'ADMIN'
//    AND u.segment = :segment
//    AND (u.department = :department OR :department IS NULL OR u.department IS NULL)
//    AND u.active = true
//""")
//    List<User> findActiveAdminsBySegmentAndDepartment(
//            @Param("segment") Segment segment,
//            @Param("department") String department
//    );
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    @Query("SELECT u.active FROM User u WHERE u.username = :username")
    Optional<Boolean> findActiveByUsername(@Param("username") String username);
}
