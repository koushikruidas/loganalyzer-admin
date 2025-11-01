package com.autumn.loganalyzer_admin.service.interfaces;

import com.autumn.loganalyzer_admin.model.KeycloakUserCheckDTO;

import java.util.Optional;

public interface KeycloakService {
    KeycloakUserCheckDTO checkUserExists(String username, String email);
    String createUser(String username, String email, boolean enabled);
    void deleteUser(String userId);
    void setTemporaryPassword(String userId, String password);
}
