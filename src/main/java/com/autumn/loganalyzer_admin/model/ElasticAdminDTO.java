package com.autumn.loganalyzer_admin.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ElasticAdminDTO {
    private String indexName;
}
