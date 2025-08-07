package com.autumn.loganalyzer_admin.controller;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.common.acl.AclBinding;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/kafka")
@RequiredArgsConstructor
public class KafkaResourceController {
    private final AdminClient adminClient;

    @GetMapping("/topics")
    public ResponseEntity<Set<String>> listTopics() throws ExecutionException, InterruptedException {
        Set<String> topics = adminClient.listTopics(new ListTopicsOptions().listInternal(false)).names().get();
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/acls")
    public ResponseEntity<Set<String>> listAcls() throws ExecutionException, InterruptedException {
        Set<String> aclStrings = adminClient.describeAcls(org.apache.kafka.common.acl.AclBindingFilter.ANY)
            .values()
            .get()
            .stream()
            .map(AclBinding::toString)
            .collect(Collectors.toSet());
        return ResponseEntity.ok(aclStrings);
    }
}
