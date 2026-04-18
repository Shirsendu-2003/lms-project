package com.example.lms.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.*;

@RestController
@RequestMapping("/api/files")
@CrossOrigin
public class FilesController {

    private final Path medicalUploadDir = Paths.get("C:/lms/uploads/medical");


    @GetMapping("/medical/{filename:.+}")
    public ResponseEntity<Resource> serveMedicalFile(@PathVariable String filename) {
        try {
            Path file = medicalUploadDir.resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            // Determine content type (basic)
            String contentType = Files.probeContentType(file);
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/medical/{filename}/download")
    public ResponseEntity<Resource> downloadMedical(@PathVariable String filename) {
        try {
            Path filePath = medicalUploadDir.resolve(filename).normalize();
            File file = filePath.toFile();

            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }

            Resource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}
