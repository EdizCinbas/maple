package dev.ediz.maple.repository;

import dev.ediz.maple.model.Account;
import dev.ediz.maple.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository
        extends JpaRepository<VerificationToken, Long> {

    VerificationToken findByToken(String token);

    VerificationToken findByAccount(Account account);
}