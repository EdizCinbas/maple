package dev.ediz.maple.config;

import dev.ediz.maple.model.Account;
import dev.ediz.maple.model.Authority;
import dev.ediz.maple.model.Post;
import dev.ediz.maple.repository.AuthorityRepository;
import dev.ediz.maple.service.AccountService;
import dev.ediz.maple.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class SeedData implements CommandLineRunner {

    @Autowired
    private PostService postService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Override
    public void run(String... args) throws Exception {
        List<Post> posts = postService.getAll();

        if (posts.isEmpty()) {

            Authority user = new Authority();
            user.setName("ROLE_USER");
            authorityRepository.save(user);

            Authority admin = new Authority();
            admin.setName("ROLE_ADMIN");
            authorityRepository.save(admin);

            Account account = new Account();
            account.setFirstName("admin");
            account.setLastName("admin");
            account.setEmail("admin.admin@domain.com");
            account.setPassword("password");
            account.setEnabled(true);
            Set<Authority> authorities = new HashSet<>();
            authorityRepository.findById("ROLE_USER").ifPresent(authorities::add);
            authorityRepository.findById("ROLE_ADMIN").ifPresent(authorities::add);
            account.setAuthorities(authorities);
            accountService.save(account);

            Post post1 = new Post();
            post1.setTitle("Title of post 1");
            post1.setBody("Body of post 1");
            post1.setAccount(account);

            Post post2 = new Post();
            post2.setTitle("Title of post 2");
            post2.setBody("Body of post 2");
            post2.setAccount(account);

            postService.save(post1);
            postService.save(post2);
        }
    }
}