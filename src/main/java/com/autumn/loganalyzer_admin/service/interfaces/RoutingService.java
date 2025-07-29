package com.autumn.loganalyzer_admin.service.interfaces;

import com.autumn.loganalyzer_admin.model.RegistrationDTO;

public interface RoutingService {
    String resolveTopic(RegistrationDTO dto);
    String resolveIndex(RegistrationDTO dto);
}
