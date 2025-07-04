package com.votingapp.votingapp.poll.pojo;

import lombok.Data;

@Data
public class VoteDeleteResponse {
    private boolean success;
    private String message;

    public VoteDeleteResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
