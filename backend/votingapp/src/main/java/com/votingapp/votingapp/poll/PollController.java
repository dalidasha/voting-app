package com.votingapp.votingapp.poll;

import com.votingapp.votingapp.poll.dto.PollDto;
import com.votingapp.votingapp.poll.pojo.*;
import com.votingapp.votingapp.user.User;
import com.votingapp.votingapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/polls")
@RequiredArgsConstructor
public class PollController {
    private final PollService pollService;
    private final UserRepository userRepository;

    @GetMapping
    public PollsResponse getAllPolls(Principal principal) {
        Optional<User> user = userRepository.findByUsername(principal.getName());

        if (user.isEmpty()) {
            return new PollsResponse(false, Collections.emptyList(), "Token is invalid");
        }

        List<PollDto> polls = pollService.getAllPolls(principal.getName());
        return new PollsResponse(true, polls, "Polls retrieved successfully");
    }

    @GetMapping("/{pollId}")
    public PollsResponse getPoll(Principal principal, @PathVariable Long pollId) {
        Optional<User> user = userRepository.findByUsername(principal.getName());

        if (user.isEmpty()) {
            return new PollsResponse(false, Collections.emptyList(), "Token is invalid");
        }

        Optional<PollDto> poll = pollService.getPoll(pollId, principal.getName());

        if (poll.isEmpty()) {
            return new PollsResponse(false, Collections.emptyList(), "No poll with such id");
        }

        return new PollsResponse(true, List.of(poll.get()), "Poll retrieved successfully");
    }

    @PostMapping
    public PollCreateResponse createPoll(@RequestBody PollCreateRequest request, Principal principal) {
        Optional<User> user = userRepository.findByUsername(principal.getName());

        if (user.isEmpty()) {
            return new PollCreateResponse(false, -1, "Token is invalid");
        }

        return pollService.createPoll(request, user.get());
    }

    @DeleteMapping("/{id}")
    public PollDeleteResponse deletePoll(@PathVariable Long id, Principal principal) {
        Optional<User> user = userRepository.findByUsername(principal.getName());

        if (user.isEmpty()) {
            return new PollDeleteResponse(false, "Token is invalid");
        }

        return pollService.deletePoll(id, user.get());
    }

    @PostMapping("/vote")
    public VoteResponse vote(Principal principal, @RequestBody VoteRequest voteRequest) {
        Optional<User> user = userRepository.findByUsername(principal.getName());

        if (user.isEmpty()) {
            return new VoteResponse(false, "Token is invalid");
        }

        return pollService.vote(voteRequest.getPollId(), voteRequest, user.get());
    }

    @DeleteMapping("/vote/{pollId}")
    public VoteDeleteResponse cancelVote(Principal principal,
                                         @PathVariable Long pollId) {
        Optional<User> user = userRepository.findByUsername(principal.getName());

        if (user.isEmpty()) {
            return new VoteDeleteResponse(false, "Token is invalid");
        }

        return pollService.cancelVote(pollId, principal.getName());
    }
}