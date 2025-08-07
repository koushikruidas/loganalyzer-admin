package com.autumn.loganalyzer_admin.service.interfaces;

import com.autumn.loganalyzer_admin.model.RegistrationRequestDTO;

public interface RoutingService {
    String resolveTopic(RegistrationRequestDTO dto);
    String resolveIndex(RegistrationRequestDTO dto);
}
