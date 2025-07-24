package com.autumn.loganalyzer_admin.service.interfaces;

import com.autumn.loganalyzer_admin.model.RegistrationDTO;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.ExecutionException;

public interface RegistrationService {
    ResponseEntity<String> registerApplication(RegistrationDTO dto) throws ExecutionException, InterruptedException;
}
