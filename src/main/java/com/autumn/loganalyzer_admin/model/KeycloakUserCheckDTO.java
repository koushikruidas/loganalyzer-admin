package com.autumn.loganalyzer_admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakUserCheckDTO {
    public boolean usernameExists;
    public boolean emailExists;
    public String userId;
}
