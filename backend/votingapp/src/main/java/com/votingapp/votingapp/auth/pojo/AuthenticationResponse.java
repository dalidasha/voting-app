package com.votingapp.votingapp.auth.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticationResponse {
    private boolean success;
    private String message;
}
