package dev.ediz.maple.config;

import dev.ediz.maple.model.Account;
import dev.ediz.maple.model.Post;
import dev.ediz.maple.service.AccountService;
import dev.ediz.maple.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SeedData implements CommandLineRunner {

    @Autowired
    private PostService postService;

    @Autowired
    private AccountService accountService;

    @Override
    public void run(String... args) throws Exception {
        List<Post> posts = postService.getAll();

        if (posts.size() == 0) {

            Account account1 = new Account();
            Account account2 = new Account();

            account1.setFirstName("user");
            account1.setLastName("user");
            account1.setEmail("user.user@domain.com");
            account1.setPassword("password");

            account2.setFirstName("admin");
            account2.setLastName("admin");
            account2.setEmail("admin.admin@domain.com");
            account2.setPassword("password");

            accountService.save(account1);
            accountService.save(account2);



            Post post1 = new Post();
            Post post2 = new Post();

            post1.setTitle("Title of post 1");
            post1.setBody("Body of post 1");
            post1.setAccount(account1);

            post2.setTitle("Title of post 2");
            post2.setBody("Body of post 2");
            post2.setAccount(account2);

            postService.save(post1);
            postService.save(post2);
        }
    }
}