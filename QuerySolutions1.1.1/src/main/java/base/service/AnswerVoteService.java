package base.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import base.dao.AnswerRepo;
import base.dao.AnswerVotesRepo;
import base.model.AnswerVotes;

@Service
public class AnswerVoteService {
	
	@Autowired
	AnswerRepo answerRepo;
	@Autowired
	AnswerVotesRepo answerVotesRepo;
	@Autowired
	AnswerRepo articleRepo;

	public int makeVoteCount(String publisherEmail, Long ansId, Long queId, int likeOrDislike) {
		/*
		 * makeVoteCount will be returning 3 values 
		 * 			0 -> if publisher isn't eligble to vote
		 * 			1 -> if publisher has already given the vote as like - then the vote will be disabled 
		 * 			2 -> if publisher is voting for the first time.
		 * 			3 -> if publisher changes his vote.
		 * 			
		 */
		
		//when publisher isn't eligble
		
		if((articleRepo.findByPublisherEmail(publisherEmail).size() < 2) || (answerRepo.findByPublisherEmail(publisherEmail).size() < 2)) {
			return 0;
		
		}//when the publisher has already given the same vote earlier
		
		else if(checkPubliserHasVotedEarlier(publisherEmail, ansId, queId)) {
		
			//discard publisher's vote if is the same vote again
			Optional<AnswerVotes> vote = answerVotesRepo.checkSign(publisherEmail, ansId, queId);
			Long voteId = answerVotesRepo.findByPublisherEmailAndAnswerId(publisherEmail, ansId).get().getVoteId();
			
			if(vote.get().getSign() == likeOrDislike) {
				answerVotesRepo.deleteById(voteId);
			}else {
				answerVotesRepo.updatePublisherVote(likeOrDislike, voteId);
				return 3;
			}
			
			
			return 1;
		
		}//make your vote count 
		
		else {
			
			AnswerVotes vote = new AnswerVotes();
			vote.setAnswerId(ansId);
			vote.setQuestionId(queId);
			vote.setPublisherEmail(publisherEmail);
			vote.setSign(likeOrDislike);
			answerVotesRepo.save(vote);
			
			return 2;
		}
	}

	private boolean checkPubliserHasVotedEarlier(String publisherEmail, Long ansId, Long queId) {
		
		Optional<AnswerVotes> answerVote = answerVotesRepo.checkSign(publisherEmail, ansId, queId);
		
		if(answerVote.isPresent()) {
			return true;
		}else {
			return false;
		}
	}

}
