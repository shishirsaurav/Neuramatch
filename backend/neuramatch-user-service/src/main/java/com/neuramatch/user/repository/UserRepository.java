package com.neuramatch.user.repository;

import com.neuramatch.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find by email
    Optional<User> findByEmail(String email);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find by role
    List<User> findByRole(User.Role role);

    // Find by status
    List<User> findByStatus(User.UserStatus status);

    // Find active users
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' AND u.emailVerified = true")
    List<User> findAllActiveUsers();

    // Find by verification token
    Optional<User> findByVerificationToken(String token);

    // Find by reset token
    Optional<User> findByResetToken(String token);

    // Find users created after date
    List<User> findByCreatedAtAfter(LocalDateTime date);

    // Find locked accounts
    @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil > CURRENT_TIMESTAMP")
    List<User> findLockedAccounts();

    // Find by company (for recruiters)
    List<User> findByCompanyNameIgnoreCase(String companyName);

    // Count by role
    long countByRole(User.Role role);

    // Count by status
    long countByStatus(User.UserStatus status);

    // Search users
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);
}
