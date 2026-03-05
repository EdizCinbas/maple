package dev.ediz.maple.service;

import dev.ediz.maple.exception.UserAlreadyExistException;
import dev.ediz.maple.model.Account;
// import dev.ediz.maple.model.VerificationToken;
import dev.ediz.maple.repository.AccountRepository;
// import dev.ediz.maple.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class AccountService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    public Account save(Account account) {
        if (findByEmail(account.getEmail()).isPresent()) {
            throw new UserAlreadyExistException("There is an account with that email address: "
                    + account.getEmail());
        }
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        return accountRepository.save(account);
    }

    public Optional<Account> findByEmail(String email) {
        return accountRepository.findOneByEmail(email);
    }

}
