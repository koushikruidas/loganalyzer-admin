package com.autumn.loganalyzer_admin.service;

import com.autumn.loganalyzer_admin.model.KeycloakUserCheckDTO;
import com.autumn.loganalyzer_admin.service.interfaces.KeycloakService;
import jakarta.ws.rs.core.Response;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KeycloakServiceImpl implements KeycloakService {

    private final Keycloak keycloak;
    private final String realm;

    public KeycloakServiceImpl(@Value("${keycloak.server-url}") String serverUrl,
                               @Value("${keycloak.realm}") String realm,
                               @Value("${keycloak.admin.username}") String adminUsername,
                               @Value("${keycloak.admin.password}") String adminPassword,
                               @Value("${keycloak.admin.client-id}") String adminClientId,
                               @Value("${keycloak.admin.client-secret}") String adminClientSecret) {
        this.realm = realm;
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .clientId(adminClientId)
                .clientSecret(adminClientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }

    @Override
    public KeycloakUserCheckDTO checkUserExists(String username, String email) {
        KeycloakUserCheckDTO result = new KeycloakUserCheckDTO();

        // 🔍 Check by username
        List<UserRepresentation> usersByUsername =
                keycloak.realm(realm).users().search(username, true);

        if (usersByUsername != null) {
            for (UserRepresentation u : usersByUsername) {
                if (username.equalsIgnoreCase(u.getUsername())) {
                    result.usernameExists = true;
                    result.userId = u.getId();
                    break;
                }
            }
        }

        // 🔍 Check by email
        List<UserRepresentation> usersByEmail =
                keycloak.realm(realm).users().search(null, null, null, email, null, null);

        if (usersByEmail != null && !usersByEmail.isEmpty()) {
            result.emailExists = true;

            // Prefer keeping real ID if user is same (username+email match)
            if (result.userId == null) {
                result.userId = usersByEmail.get(0).getId();
            }
        }

        return result;
    }


    @Override
    public String createUser(String username, String email, boolean enabled) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setEnabled(enabled);

        Response response = keycloak.realm(realm).users().create(user);
        if (response.getStatus() != 201 && response.getStatus() != 204) {
            String err = String.format("Keycloak create user failed: status=%d, msg=%s",
                    response.getStatus(), response.getStatusInfo());
            response.close();
            throw new RuntimeException(err);
        }
        String location = response.getLocation().toString();
        response.close();
        String createdId = location.substring(location.lastIndexOf('/') + 1);
        return createdId;
    }

    @Override
    public void deleteUser(String userId) {
        keycloak.realm(realm).users().delete(userId);
    }

    @Override
    public void setTemporaryPassword(String userId, String password) {
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setTemporary(false);
        cred.setValue(password);
        keycloak.realm(realm).users().get(userId).resetPassword(cred);
    }
}
