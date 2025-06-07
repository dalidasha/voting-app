package com.votingapp.votingapp.poll.pojo;

import lombok.Data;

@Data
public class PollCreateResponse {
    private long pollId;
    private boolean success;
    private String message;

    public PollCreateResponse(boolean success, long pollId, String message) {
        this.success = success;
        this.pollId = pollId;
        this.message = message;
    }
}
