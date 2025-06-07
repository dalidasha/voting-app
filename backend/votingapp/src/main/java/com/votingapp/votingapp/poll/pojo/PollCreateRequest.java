package com.votingapp.votingapp.poll.pojo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PollCreateRequest {
    private String name;
    private String description;
    private LocalDate endDate;
    private List<QuestionCreateRequest> questions;
}
