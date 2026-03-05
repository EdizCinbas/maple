package dev.ediz.maple.controller;

import dev.ediz.maple.service.MediaUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class MediaManagerController {

    @Autowired
    private MediaUploadService mediaUploadService;

    @GetMapping("/media")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String mediaManager(Model model) throws IOException {
        model.addAttribute("files", mediaUploadService.listFiles());
        return "media";
    }
}
