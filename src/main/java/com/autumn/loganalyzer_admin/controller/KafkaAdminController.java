package com.autumn.loganalyzer_admin.controller;

import com.autumn.loganalyzer_admin.service.interfaces.KafkaAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/kafka/topic")
@RequiredArgsConstructor
public class KafkaAdminController {

    private final KafkaAdminService kafkaAdminService;

    @DeleteMapping("/delete/{topicName}")
    public ResponseEntity<String> deleteTopic(@PathVariable String topicName) {
        try {
            kafkaAdminService.deleteTopic(topicName);
            return ResponseEntity.ok("Topic deleted successfully: " + topicName);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete topic: " + e.getMessage());
        }
    }
}

