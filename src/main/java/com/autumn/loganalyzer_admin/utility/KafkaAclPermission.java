package com.autumn.loganalyzer_admin.utility;

import org.apache.kafka.common.acl.AclOperation;

public enum KafkaAclPermission {
    READ, WRITE, DELETE, DESCRIBE;

    public AclOperation toAclOperation() {
        return switch (this) {
            case READ -> AclOperation.READ;
            case WRITE -> AclOperation.WRITE;
            case DELETE -> AclOperation.DELETE;
            case DESCRIBE -> AclOperation.DESCRIBE;
        };
    }
}