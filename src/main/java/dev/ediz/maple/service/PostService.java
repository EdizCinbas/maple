package dev.ediz.maple.service;

import dev.ediz.maple.repository.PostRepository;
import dev.ediz.maple.model.Post;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private static final Parser MD_PARSER = Parser.builder()
            .extensions(List.of(TablesExtension.create(), ImageAttributesExtension.create()))
            .build();

    private static final HtmlRenderer MD_RENDERER = HtmlRenderer.builder()
            .extensions(List.of(TablesExtension.create(), ImageAttributesExtension.create()))
            .build();

    private static final TextContentRenderer MD_TEXT_RENDERER = TextContentRenderer.builder().build();

    @Autowired
    private PostRepository postRepository;

    public Optional<Post> getById(Long id) {
        return postRepository.findById(id);
    }

    public List<Post> getAll() {
        return postRepository.findAll();
    }

    public Post save(Post post) {
        if (post.getId() == null) {
            post.setCreatedAt(LocalDateTime.now());
        }
        post.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    public void delete(Post post) {
        postRepository.delete(post);
    }

    public String renderMarkdown(String markdown) {
        if (markdown == null) return "";
        Node document = MD_PARSER.parse(markdown);
        return MD_RENDERER.render(document);
    }

    public String renderMarkdownToPlainText(String markdown) {
        if (markdown == null) return "";
        Node document = MD_PARSER.parse(markdown);
        return MD_TEXT_RENDERER.render(document);
    }
}
