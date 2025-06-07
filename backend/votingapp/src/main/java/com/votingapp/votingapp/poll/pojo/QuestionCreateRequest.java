package com.votingapp.votingapp.poll.pojo;

import lombok.Data;

import java.util.List;

@Data
public class QuestionCreateRequest {
    private String text;
    private List<AnswerCreateRequest> answers;
}
