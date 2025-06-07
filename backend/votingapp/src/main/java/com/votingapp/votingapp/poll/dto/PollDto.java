package com.votingapp.votingapp.poll.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PollDto {
    private Long id;
    private String name;
    private String description;
    private LocalDate endDate;
    private List<QuestionDto> questions;
    private boolean voted;
    private int voteCount;
}

