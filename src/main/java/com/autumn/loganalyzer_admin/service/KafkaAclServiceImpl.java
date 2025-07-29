package com.autumn.loganalyzer_admin.service;

import com.autumn.loganalyzer_admin.model.KafkaAclRequest;
import com.autumn.loganalyzer_admin.service.interfaces.KafkaAclService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.acl.*;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class KafkaAclServiceImpl implements KafkaAclService {

    private final AdminClient adminClient;

    @Override
    public void addAcl(KafkaAclRequest request) {
        AclBinding aclBinding = new AclBinding(
                new ResourcePattern(
                        ResourceType.fromString(request.getResourceType().name()),
                        request.getResourceName(),
                        PatternType.LITERAL
                ),
                new AccessControlEntry(
                        "User:" + request.getUsername(),
                        "*",  // host, use "*" for all
                        request.getPermissionType().toAclOperation(),
                        AclPermissionType.ALLOW
                )
        );

        adminClient.createAcls(Collections.singleton(aclBinding)).all().whenComplete((v, ex) -> {
            if (ex != null) {
                System.err.println("Failed to create ACL: " + ex.getMessage());
            } else {
                System.out.println("ACL created successfully for user: " + request.getUsername());
            }
        });
    }
}
