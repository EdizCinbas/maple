package dev.ediz.maple.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MediaUploadService {

    // Extension is keyed by MIME type so we never trust the original filename
    private static final Map<String, String> MIME_TO_EXT = Map.of(
            "image/jpeg",  ".jpg",
            "image/png",   ".png",
            "image/gif",   ".gif",
            "image/webp",  ".webp",
            "video/mp4",   ".mp4",
            "video/webm",  ".webm"
    );

    @Value("${app.upload.max-bytes:52428800}")
    private long maxBytes;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public String store(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !MIME_TO_EXT.containsKey(contentType)) {
            throw new IllegalArgumentException(
                    "Unsupported file type: " + contentType +
                    ". Allowed: JPEG, PNG, GIF, WebP images and MP4/WebM videos.");
        }

        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException(
                    "File too large. Maximum allowed size is " + (maxBytes / 1024 / 1024) + " MB.");
        }

        String filename = UUID.randomUUID() + MIME_TO_EXT.get(contentType);

        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        // Path-traversal guard
        Path destination = dir.resolve(filename).normalize();
        if (!destination.startsWith(dir)) {
            throw new SecurityException("Attempted path traversal in filename: " + filename);
        }

        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + filename;
    }

    public List<MediaFile> listFiles() throws IOException {
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(dir)) return List.of();

        try (var stream = Files.list(dir)) {
            return stream
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> {
                        long modified = 0;
                        try { modified = Files.getLastModifiedTime(p).toMillis(); } catch (IOException ignored) {}
                        return new MediaFile(p.getFileName().toString(), modified);
                    })
                    .sorted((a, b) -> Long.compare(b.lastModified(), a.lastModified()))
                    .toList();
        }
    }

    public void delete(String filename) throws IOException {
        if (filename == null || filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            throw new SecurityException("Invalid filename: " + filename);
        }
        if (!filename.matches("[a-f0-9\\-]+\\.(jpg|png|gif|webp|mp4|webm)")) {
            throw new SecurityException("Filename does not match expected pattern: " + filename);
        }

        Path dir  = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path file = dir.resolve(filename).normalize();

        if (!file.startsWith(dir)) {
            throw new SecurityException("Attempted path traversal: " + filename);
        }

        Files.deleteIfExists(file);
    }

    public record MediaFile(String filename, long lastModified, String url, boolean video) {
        public MediaFile(String filename, long lastModified) {
            this(filename, lastModified,
                 "/uploads/" + filename,
                 filename.endsWith(".mp4") || filename.endsWith(".webm"));
        }
    }
}
