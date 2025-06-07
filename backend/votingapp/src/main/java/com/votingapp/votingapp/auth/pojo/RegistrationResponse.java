package com.votingapp.votingapp.auth.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegistrationResponse {
    private boolean success;
    private String message;
}