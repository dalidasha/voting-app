package com.votingapp.votingapp.poll;

import com.votingapp.votingapp.poll.dto.AnswerDto;
import com.votingapp.votingapp.poll.dto.PollDto;
import com.votingapp.votingapp.poll.dto.QuestionDto;
import com.votingapp.votingapp.poll.entities.Answer;
import com.votingapp.votingapp.poll.entities.Poll;
import com.votingapp.votingapp.poll.entities.Question;
import com.votingapp.votingapp.poll.entities.Vote;
import com.votingapp.votingapp.poll.pojo.*;
import com.votingapp.votingapp.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PollService {
    private final PollRepository pollRepository;
    private final VoteRepository voteRepository;

    public List<PollDto> getAllPolls(String userEmail) {
        List<Poll> polls = pollRepository.findAll();

        return polls.stream()
                .map(poll -> convertToDto(poll, userEmail))
                .collect(Collectors.toList());
    }

    public Optional<PollDto> getPoll(Long pollId, String userEmail) {
        Optional<Poll> pollOptional = pollRepository.findById(pollId);

        if (pollOptional.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(convertToDto(pollOptional.get(), userEmail));
    }

    @Transactional
    public VoteResponse vote(Long pollId, VoteRequest voteRequest, User user) {
        Optional<Poll> pollOptional = pollRepository.findById(pollId);

        if (pollOptional.isEmpty()) {
            return new VoteResponse(false, "No poll with such id");
        }

        Poll poll = pollOptional.get();

        if (!poll.isActive()) {
            return new VoteResponse(false, "This poll is not active");
        }

        if (voteRepository.existsByUserEmailAndPoll(user.getUsername(), poll)) {
            return new VoteResponse(false, "You already voted for this poll");
        }

        if (!validateAnswers(poll, voteRequest.getAnswerIds())) {
            return new VoteResponse(false, "Wrong answers to poll questions. Voting is forbidden");
        }

        Vote vote = new Vote();
        vote.setUserEmail(user.getUsername());
        vote.setPoll(poll);
        vote.setVotedAt(LocalDateTime.now());
        vote.setUser(user);

        voteRepository.save(vote);

        return new VoteResponse(true, "Voted successfully");
    }

    @Transactional
    public VoteDeleteResponse cancelVote(Long pollId, String userEmail) {
        Optional<Poll> pollOptional = pollRepository.findById(pollId);

        if (pollOptional.isEmpty()) {
            return new VoteDeleteResponse(false, "No poll with such id");
        }

        Poll poll = pollOptional.get();

        Optional<Vote> voteOptional = voteRepository.findByUserEmailAndPoll(userEmail, poll);
        if (voteOptional.isEmpty()) {
            return new VoteDeleteResponse(false, "You haven't voted for this poll");
        }

        voteRepository.delete(voteOptional.get());

        return new VoteDeleteResponse(true, "Vote cancelled successfully");
    }

    @Transactional
    public PollCreateResponse createPoll(PollCreateRequest request, User user) {
        if (Objects.isNull(request.getName()) || request.getName().isBlank()) {
            return new PollCreateResponse(false, -1, "Poll's name cannot be empty");
        }

        if (Objects.isNull(request.getEndDate()) || request.getEndDate().isBefore(LocalDateTime.now().toLocalDate())) {
            return new PollCreateResponse(false, -1, "Invalid date");
        }

        if (Objects.isNull(request.getQuestions()) || request.getQuestions().size() != 3) {
            return new PollCreateResponse(false, -1, "Invalid questions");
        }

        for (QuestionCreateRequest questionCreateRequest: request.getQuestions()) {
            if (Objects.isNull(questionCreateRequest.getAnswers()) || questionCreateRequest.getAnswers().size() != 3) {
                return new PollCreateResponse(false, -1, "Invalid answers");
            }

            int correctCount = 0;

            for (AnswerCreateRequest answerCreateRequest: questionCreateRequest.getAnswers()) {
                if (answerCreateRequest.isCorrect()) {
                    correctCount++;
                }
            }

            if (correctCount != 1) {
                return new PollCreateResponse(false, -1, "Invalid answers");
            }
        }

        Poll poll = new Poll();
        poll.setName(request.getName());
        poll.setDescription(request.getDescription());
        poll.setEndDate(request.getEndDate());
        poll.setAuthor(user);

        request.getQuestions().forEach(questionRequest -> {
            Question question = new Question();
            question.setText(questionRequest.getText());

            questionRequest.getAnswers().forEach(answerRequest -> {
                Answer answer = new Answer();
                answer.setText(answerRequest.getText());
                answer.setCorrect(answerRequest.isCorrect());
                question.addAnswer(answer);
            });

            poll.addQuestion(question);
        });

       Poll createdPoll = pollRepository.save(poll);

       return new PollCreateResponse(true, createdPoll.getId(), "Poll created successfully");
    }

    @Transactional
    public PollDeleteResponse deletePoll(Long pollId, User user) {
        Optional<Poll> poll = pollRepository.findById(pollId);

        if (poll.isEmpty()) {
            return new PollDeleteResponse(false, "No poll with such id");
        }

        if (!poll.get().getAuthor().getId().equals(user.getId())) {
            return new PollDeleteResponse(false, "User doesn't have privileges to delete this poll");
        }

        pollRepository.delete(poll.get());

        return new PollDeleteResponse(true, "Poll deleted successfully");
    }

    private boolean validateAnswers(Poll poll, List<Long> answerIds) {
        if (answerIds.size() != poll.getQuestions().size()) {
            return false;
        }

        List<Answer> correctAnswers = poll.getQuestions().stream()
                .flatMap(question -> question.getAnswers().stream())
                .filter(Answer::isCorrect)
                .collect(Collectors.toList());

        List<Long> correctAnswerIds = correctAnswers.stream()
                .map(Answer::getId)
                .collect(Collectors.toList());

        return answerIds.containsAll(correctAnswerIds) && correctAnswerIds.containsAll(answerIds);
    }

    private PollDto convertToDto(Poll poll, String userEmail) {
        PollDto dto = new PollDto();
        dto.setId(poll.getId());
        dto.setName(poll.getName());
        dto.setDescription(poll.getDescription());
        dto.setEndDate(poll.getEndDate());
        dto.setVoted(voteRepository.existsByUserEmailAndPoll(userEmail, poll));
        dto.setVoteCount(poll.getVoteCount());

        List<QuestionDto> questionDtos = new ArrayList<>();
        if (poll.getQuestions() != null) {
            for (Question question : poll.getQuestions()) {
                QuestionDto questionDto = new QuestionDto();
                questionDto.setId(question.getId());
                questionDto.setText(question.getText());

                List<AnswerDto> answerDtos = new ArrayList<>();
                if (question.getAnswers() != null) {
                    for (Answer answer : question.getAnswers()) {
                        AnswerDto answerDto = new AnswerDto();
                        answerDto.setId(answer.getId());
                        answerDto.setText(answer.getText());
                        answerDtos.add(answerDto);
                    }
                }

                questionDto.setAnswers(answerDtos);
                questionDtos.add(questionDto);
            }
        }

        dto.setQuestions(questionDtos);

        return dto;
    }
}
