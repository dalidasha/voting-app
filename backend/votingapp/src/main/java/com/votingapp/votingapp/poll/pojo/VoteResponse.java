package com.votingapp.votingapp.poll.pojo;

import lombok.Data;

@Data
public class VoteResponse {
    private boolean success;
    private String message;

    public VoteResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
