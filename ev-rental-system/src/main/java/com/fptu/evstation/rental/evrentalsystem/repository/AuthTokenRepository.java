package com.fptu.evstation.rental.evrentalsystem.repository;

import com.fptu.evstation.rental.evrentalsystem.entity.AuthToken;
import com.fptu.evstation.rental.evrentalsystem.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
@Transactional
public interface AuthTokenRepository extends JpaRepository<AuthToken,Integer> {
    public Optional<AuthToken> findByToken(String token);
    @Transactional
    public void deleteByToken(AuthToken token);


    @Modifying
    @Transactional
    @Query("delete from AuthToken t where t.expiresAt < :now")
    int deleteExpiredBefore(@Param("now") Instant now);
}
