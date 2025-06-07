package com.votingapp.votingapp.poll;

import com.votingapp.votingapp.poll.entities.Poll;
import com.votingapp.votingapp.poll.entities.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByUserEmailAndPoll(String userEmail, Poll poll);

    void deleteByUserEmailAndPoll(String userEmail, Poll poll);

    boolean existsByUserEmailAndPoll(String userEmail, Poll poll);
}
