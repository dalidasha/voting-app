package com.votingapp.votingapp.poll.pojo;

import lombok.Data;

@Data
public class AnswerCreateRequest {
    private String text;
    private boolean correct;
}
