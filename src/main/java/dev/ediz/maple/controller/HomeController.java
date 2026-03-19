package dev.ediz.maple.controller;

import dev.ediz.maple.model.Post;
import dev.ediz.maple.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private PostService postService;

    @GetMapping("/")
    public String portfolio(Model model) {
        List<Post> posts = postService.getAll();
        // Limit to 3 most recent posts
        int limit = Math.min(posts.size(), 3);
        List<Post> recentPosts = posts.subList(0, limit);
        
        Map<Long, String> excerpts = new LinkedHashMap<>();
        for (Post post : recentPosts) {
            String excerpt = postService.renderExcerpt(post.getBody(), 200);
            excerpts.put(post.getId(), excerpt);
        }
        model.addAttribute("posts", recentPosts);
        model.addAttribute("excerpts", excerpts);
        return "portfolio";
    }

    @GetMapping("/blog")
    public String blog(Model model) {
        List<Post> posts = postService.getAll();
        Map<Long, String> excerpts = new LinkedHashMap<>();
        for (Post post : posts) {
            String excerpt = postService.renderExcerpt(post.getBody(), 200);
            excerpts.put(post.getId(), excerpt);
        }
        model.addAttribute("posts", posts);
        model.addAttribute("excerpts", excerpts);
        return "home";
    }
}
