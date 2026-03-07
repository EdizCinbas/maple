package dev.ediz.maple.repository;

import dev.ediz.maple.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findOneByEmail(String email);

    @Query("SELECT COUNT(a) > 0 FROM Account a JOIN a.authorities auth WHERE auth.name = 'ROLE_ADMIN'")
    boolean existsAdminAccount();
}
