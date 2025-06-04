package com.example.signalserver.repository.user;

import com.example.signalserver.model.entity.User;
import com.example.signalserver.model.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Основные методы поиска
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Поиск по username или email (для аутентификации)
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    // Методы для работы со статусом пользователей
    List<User> findByStatus(UserStatus status);

    List<User> findByStatusAndOnlineTrue(UserStatus status);

    @Query("SELECT u FROM User u WHERE u.online = true AND u.status = 'ACTIVE'")
    List<User> findActiveOnlineUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.online = true AND u.status = 'ACTIVE'")
    long countActiveOnlineUsers();

    // Поиск пользователей с пагинацией
    Page<User> findByStatusOrderByCreatedAtDesc(UserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.status = :status ORDER BY u.lastSeenAt DESC")
    Page<User> findByStatusOrderByLastSeenDesc(@Param("status") UserStatus status, Pageable pageable);

    // Поиск по имени (поиск пользователей)
    @Query("SELECT u FROM User u WHERE " +
            "(LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "u.status = 'ACTIVE'")
    Page<User> searchActiveUsers(@Param("search") String search, Pageable pageable);

    // Методы для обновления активности
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.online = :online, u.lastSeenAt = :lastSeen WHERE u.id = :userId")
    void updateUserOnlineStatus(@Param("userId") Long userId,
                                @Param("online") boolean online,
                                @Param("lastSeen") LocalDateTime lastSeen);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastSeenAt = :lastSeen WHERE u.username = :username")
    void updateLastSeenByUsername(@Param("username") String username,
                                  @Param("lastSeen") LocalDateTime lastSeen);

    // Методы для верификации email
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.status = 'ACTIVE'")
    List<User> findUnverifiedUsers();

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.id = :userId")
    void verifyEmailById(@Param("userId") Long userId);

    // Статистические запросы
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatus(@Param("status") UserStatus status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :fromDate")
    long countUsersRegisteredAfter(@Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT COUNT(u) FROM User u WHERE u.lastSeenAt >= :fromDate AND u.status = 'ACTIVE'")
    long countActiveUsersSince(@Param("fromDate") LocalDateTime fromDate);

    // Методы для работы с комнатами
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.callSessions cs " +
            "WHERE cs.room.id = :roomId AND cs.active = true")
    List<User> findActiveUsersInRoom(@Param("roomId") Long roomId);

    @Query("SELECT u FROM User u WHERE u.id IN " +
            "(SELECT DISTINCT cs.user.id FROM CallSession cs " +
            "WHERE cs.room.roomCode = :roomCode AND cs.active = true)")
    List<User> findActiveUsersInRoom(@Param("roomCode") String roomCode);

    // Методы для очистки неактивных пользователей
    @Query("SELECT u FROM User u WHERE u.online = true AND u.lastSeenAt < :cutoffTime")
    List<User> findStaleOnlineUsers(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.online = false WHERE u.lastSeenAt < :cutoffTime AND u.online = true")
    int markStaleUsersOffline(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Методы для администрирования
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE' ORDER BY u.createdAt DESC")
    Page<User> findActiveUsersOrderByRegistration(Pageable pageable);

    @Query("SELECT u FROM User u ORDER BY u.lastSeenAt DESC")
    Page<User> findAllOrderByLastActivity(Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.status = :status WHERE u.id = :userId")
    void updateUserStatus(@Param("userId") Long userId, @Param("status") UserStatus status);

    // Методы для работы с массовыми операциями
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.online = false WHERE u.id IN :userIds")
    void setUsersOffline(@Param("userIds") List<Long> userIds);

    List<User> findByIdIn(List<Long> userIds);

    // Дополнительные полезные методы
    @Query("SELECT u FROM User u WHERE u.avatarUrl IS NOT NULL AND u.status = 'ACTIVE'")
    List<User> findUsersWithAvatars();

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.id != :excludeId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("excludeId") Long excludeId);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username AND u.id != :excludeId")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("excludeId") Long excludeId);

    // Методы для работы с друзьями (если понадобится в будущем)
    @Query(value = """
        SELECT u.* FROM users u 
        WHERE u.id IN (
            SELECT f.friend_id FROM friends f 
            WHERE f.user_id = :userId AND f.status = 'ACCEPTED'
            UNION
            SELECT f.user_id FROM friends f 
            WHERE f.friend_id = :userId AND f.status = 'ACCEPTED'
        ) AND u.status = 'ACTIVE'
        """, nativeQuery = true)
    List<User> findUserFriends(@Param("userId") Long userId);
}
