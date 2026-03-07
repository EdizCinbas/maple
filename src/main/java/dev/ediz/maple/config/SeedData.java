package dev.ediz.maple.config;

import dev.ediz.maple.model.Account;
import dev.ediz.maple.model.Authority;
import dev.ediz.maple.repository.AccountRepository;
import dev.ediz.maple.repository.AuthorityRepository;
import dev.ediz.maple.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class SeedData implements CommandLineRunner {

    @Value("${app.seed.admin.email}")
    private String adminEmail;

    @Value("${app.seed.admin.password}")
    private String adminPassword;

    @Value("${app.seed.admin.firstName}")
    private String adminFirstName;

    @Value("${app.seed.admin.lastName}")
    private String adminLastName;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Override
    public void run(String... args) {
        // Only runs if no admin account exists - ensures the app is always operable
        if (accountRepository.existsAdminAccount()) return;

        Authority roleUser = new Authority();
        roleUser.setName("ROLE_USER");
        authorityRepository.save(roleUser);

        Authority roleAdmin = new Authority();
        roleAdmin.setName("ROLE_ADMIN");
        authorityRepository.save(roleAdmin);

        Account account = new Account();
        account.setFirstName(adminFirstName);
        account.setLastName(adminLastName);
        account.setEmail(adminEmail);
        account.setPassword(adminPassword);
        account.setEnabled(true);

        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById("ROLE_USER").ifPresent(authorities::add);
        authorityRepository.findById("ROLE_ADMIN").ifPresent(authorities::add);
        account.setAuthorities(authorities);

        accountService.save(account);
    }
}