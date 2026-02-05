package com.ecommerce.project.security.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserInfoResponse {

    private Long id;
    private String username;

    private String jwtToken;

    private List<String> roles;

//    public UserInfoResponse(Long id, String username, List<String> roles, String jwtToken) {
//    }
}
