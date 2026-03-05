package dev.ediz.maple.controller;

import dev.ediz.maple.service.MediaUploadService;
import dev.ediz.maple.service.MediaUploadService.MediaFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MediaUploadController {

    @Autowired
    private MediaUploadService mediaUploadService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided."));
        }
        try {
            String url = mediaUploadService.store(file);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to save file. Please try again."));
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/media")
    public ResponseEntity<?> listMedia() {
        try {
            List<MediaFile> files = mediaUploadService.listFiles();
            return ResponseEntity.ok(files);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Could not list media files."));
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/media/{filename}")
    public ResponseEntity<?> deleteMedia(@PathVariable String filename) {
        try {
            mediaUploadService.delete(filename);
            return ResponseEntity.ok(Map.of("deleted", filename));
        } catch (SecurityException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Could not delete file."));
        }
    }
}
