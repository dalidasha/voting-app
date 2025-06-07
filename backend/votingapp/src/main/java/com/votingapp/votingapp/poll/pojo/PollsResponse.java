package com.votingapp.votingapp.poll.pojo;

import com.votingapp.votingapp.poll.dto.PollDto;
import lombok.Data;

import java.util.List;

@Data
public class PollsResponse {
    private boolean success;
    private String message;
    private List<PollDto> polls;

    public PollsResponse(boolean success, List<PollDto> polls, String message) {
        this.success = success;
        this.polls = polls;
        this.message = message;
    }
}