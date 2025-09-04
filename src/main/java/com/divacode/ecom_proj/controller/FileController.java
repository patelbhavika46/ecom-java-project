package com.divacode.ecom_proj.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final String UPLOAD_DIR = "uploads/";
    private static final String REDIS_QUEUE = "file_processing_queue";
    private static final String REDIS_STATUS_PREFIX = "file_status:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>("Please select a file to upload.", HttpStatus.BAD_REQUEST);
        }

        try {
            // 1. Generate a unique ID for the file
            String fileId = UUID.randomUUID().toString();

            // 2. Save the file to a temporary location
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(fileId + ".csv");
            Files.copy(file.getInputStream(), filePath);

            // 3. Store initial status in Redis Hash
            String statusKey = REDIS_STATUS_PREFIX + fileId;
            redisTemplate.opsForHash().put(statusKey, "status", "QUEUED");
            redisTemplate.opsForHash().put(statusKey, "filename", file.getOriginalFilename());

            // 4. Push the file ID to the Redis queue for the worker to pick up
            redisTemplate.opsForList().leftPush(REDIS_QUEUE, fileId);

            return ResponseEntity.ok("File uploaded. Processing ID: " + fileId);

        } catch (IOException e) {
            return new ResponseEntity<>("Failed to upload file.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/status/{fileId}")
    public ResponseEntity<String> getFileStatus(@PathVariable String fileId) {
        // Query the Redis hash for the status field
        String status = (String) redisTemplate.opsForHash().get(REDIS_STATUS_PREFIX + fileId, "status");

        if (status != null) {
            return ResponseEntity.ok(status);
        } else {
            // Return 404 Not Found if the file ID doesn't exist in Redis
            return new ResponseEntity<>("File ID not found", HttpStatus.NOT_FOUND);
        }
    }
}
