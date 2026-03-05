package dev.ediz.maple.service;

import dev.ediz.maple.repository.PostRepository;
import dev.ediz.maple.model.Post;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlNodeRendererFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.html.HtmlWriter;
import org.commonmark.renderer.text.TextContentRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PostService {

    private static final List<org.commonmark.Extension> EXTENSIONS =
            List.of(TablesExtension.create(), ImageAttributesExtension.create());

    private static final Parser MD_PARSER = Parser.builder()
            .extensions(EXTENSIONS)
            .build();

    // escapeHtml blocks raw <script>/<iframe> etc; VideoNodeRendererFactory carves out
    // an exception for our own <video> tags so they still render correctly.
    private static final HtmlRenderer MD_RENDERER = HtmlRenderer.builder()
            .extensions(EXTENSIONS)
            .sanitizeUrls(true)
            .escapeHtml(true)
            .nodeRendererFactory(new VideoNodeRendererFactory())
            .build();

    private static final TextContentRenderer MD_TEXT_RENDERER = TextContentRenderer.builder().build();

    // Intercepts raw HTML nodes and only lets <video src="/uploads/..."> through.
    // Everything else gets escaped as text.
    private static class VideoNodeRendererFactory implements HtmlNodeRendererFactory {

        private static final Pattern VIDEO_OPEN = Pattern.compile(
                "(?i)<video\\s+src=\"(/uploads/[a-f0-9\\-]+\\.(?:mp4|webm))\"[^>]*>",
                Pattern.CASE_INSENSITIVE
        );
        private static final Pattern VIDEO_CLOSE = Pattern.compile("(?i)</video\\s*>");

        @Override
        public NodeRenderer create(HtmlNodeRendererContext context) {
            HtmlWriter html = context.getWriter();
            return new NodeRenderer() {
                @Override
                public Set<Class<? extends Node>> getNodeTypes() {
                    return Set.of(HtmlInline.class, HtmlBlock.class);
                }

                @Override
                public void render(Node node) {
                    String literal = node instanceof HtmlInline
                            ? ((HtmlInline) node).getLiteral()
                            : ((HtmlBlock) node).getLiteral();

                    String trimmed = literal.trim();
                    Matcher open = VIDEO_OPEN.matcher(trimmed);
                    if (open.find()) {
                        html.raw("<video src=\"" + open.group(1) + "\" controls style=\"max-width:100%\"></video>");
                    } else if (!VIDEO_CLOSE.matcher(trimmed).matches()) {
                        html.text(literal);
                    }
                }
            };
        }
    }

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

    public String renderExcerpt(String markdown, int maxChars) {
        if (markdown == null) return "";
        String cleaned = markdown;

        cleaned = cleaned.replaceAll("(?is)<video[^>]*>.*?</video>", "");
        cleaned = cleaned.replaceAll("(?i)<video[^>]*>", "");
        cleaned = cleaned.replaceAll("!\\[[^]]*]\\([^)]*\\)", "");

        cleaned = cleaned.replaceAll("/uploads/\\S+", "");
        cleaned = cleaned.replaceAll("https?://\\S+", "");

        String text = renderMarkdownToPlainText(cleaned);

        text = text.replaceAll("(?i)<[^>]*>", "");
        text = text.replaceAll("[()\\[\\]{}]", "");

        text = text.replaceAll("\\s+", " ").trim();

        if (text.length() > maxChars) {
            int cut = text.lastIndexOf(' ', maxChars);
            text = text.substring(0, cut > 0 ? cut : maxChars) + "…";
        }
        return text;
    }
}
