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
    public String home(Model model) {
        List<Post> posts = postService.getAll();
        Map<Long, String> excerpts = new LinkedHashMap<>();
        for (Post post : posts) {
            String plain = postService.renderMarkdownToPlainText(post.getBody());
            excerpts.put(post.getId(), plain.length() > 200 ? plain.substring(0, 200) + "…" : plain);
        }
        model.addAttribute("posts", posts);
        model.addAttribute("excerpts", excerpts);
        return "home";
    }
}