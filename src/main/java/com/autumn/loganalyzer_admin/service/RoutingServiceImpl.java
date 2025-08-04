package com.autumn.loganalyzer_admin.service;

import com.autumn.loganalyzer_admin.model.RegistrationRequestDTO;
import com.autumn.loganalyzer_admin.service.interfaces.RoutingService;
import org.springframework.stereotype.Service;

@Service
public class RoutingServiceImpl implements RoutingService {

    @Override
    public String resolveTopic(RegistrationRequestDTO dto) {
        switch (dto.getRoutingPolicy()) {
            case ORG_WISE_TOPIC_AND_INDEX:
                return dto.getOrganizationName().toLowerCase();
            case APP_WISE_TOPIC_AND_INDEX:
                return dto.getOrganizationName().toLowerCase() + "_" + dto.getApplicationName().toLowerCase();
            case ENV_WISE_TOPIC_SHARED_INDEX:
                return dto.getOrganizationName().toLowerCase() + "_" + dto.getEnvironment().toLowerCase();
            default:
                throw new IllegalArgumentException("Unsupported routing policy");
        }
    }

    @Override
    public String resolveIndex(RegistrationRequestDTO dto) {
        switch (dto.getRoutingPolicy()) {
            case ORG_WISE_TOPIC_AND_INDEX:
                return dto.getOrganizationName().toLowerCase();
            case APP_WISE_TOPIC_AND_INDEX:
                return dto.getOrganizationName().toLowerCase() + "_" + dto.getApplicationName().toLowerCase();
            case ENV_WISE_TOPIC_SHARED_INDEX:
                return dto.getOrganizationName().toLowerCase() + "_" + dto.getEnvironment().toLowerCase();
            default:
                throw new IllegalArgumentException("Unsupported routing policy");
        }
    }
}