package dev.ediz.maple.service;

import dev.ediz.maple.exception.UserAlreadyExistException;
import dev.ediz.maple.model.Account;
import dev.ediz.maple.model.VerificationToken;
import dev.ediz.maple.repository.AccountRepository;
import dev.ediz.maple.repository.VerificationTokenRepository;
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

    @Autowired
    private VerificationTokenRepository tokenRepository;

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

    public void createVerificationToken(Account account, String token) {
        VerificationToken myToken = new VerificationToken(token, account);
        tokenRepository.save(myToken);
    }

    public VerificationToken getVerificationToken(String VerificationToken) {
        return tokenRepository.findByToken(VerificationToken);
    }

}
