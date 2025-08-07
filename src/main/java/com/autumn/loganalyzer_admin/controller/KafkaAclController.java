package com.autumn.loganalyzer_admin.controller;

import com.autumn.loganalyzer_admin.model.KafkaAclRequest;
import com.autumn.loganalyzer_admin.service.interfaces.KafkaAclService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafka/acl")
@RequiredArgsConstructor
public class KafkaAclController {

    private final KafkaAclService kafkaAclService;

    @PostMapping("/create")
    public ResponseEntity<String> createAcl(@RequestBody KafkaAclRequest request) {
        try {
            kafkaAclService.createAcls(request);
            return ResponseEntity.ok("ACL created successfully for resource: " + request.getResourceType());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create ACL: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteAcl(@RequestBody KafkaAclRequest request) {
        try {
            kafkaAclService.deleteAclsByResourceAndUser(request);
            return ResponseEntity.ok("ACL deleted successfully for resource: " + request.getResourceType());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete ACL: " + e.getMessage());
        }
    }
}
