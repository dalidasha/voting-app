package com.votingapp.votingapp.poll.pojo;

import lombok.Data;

import java.util.List;

@Data
public class VoteRequest {
    private Long pollId;
    private List<Long> answerIds;
}
