package com.votingapp.votingapp.poll.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionDto {
    private Long id;
    private String text;
    private List<AnswerDto> answers;
}