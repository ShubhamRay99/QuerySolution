package base.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import base.dao.ArticleRepo;
import base.dao.ArticleVotesRepo;
import base.model.ArticleVotes;

@Service
public class ArticleVoteService {

	@Autowired
	ArticleRepo articleRepo;
	@Autowired
	ArticleRepo answerRepo;
	@Autowired
	ArticleVotesRepo articleVotesRepo;

	public int makeVoteCountForArticle(String publisherEmail, Long artId, int likeOrDislike) {

		/*
		 * makeVoteCount will be returning 3 values 
		 * 			0 -> if publisher isn't eligble to vote
		 * 			1 -> if publisher has already given the vote as like - then the vote will be disabled 
		 * 			2 -> if publisher is voting for the first time.
		 * 			3 -> if publisher changes his vote.
		 * 			
		 */
		
		//when publisher isn't eligble
		
		if((articleRepo.findByPublisherEmail(publisherEmail).size() < 2) || 
				(answerRepo.findByPublisherEmail(publisherEmail).size() < 2)) {
		
			return 0;
		
		}//when the publisher has already given the same vote earlier
		
		else if(checkPubliserHasVotedEarlierForThisArticle(publisherEmail, artId)) {
		
			//discard publisher's vote, if he gives the same vote again
			Optional<ArticleVotes> vote = articleVotesRepo.checkSign(publisherEmail, artId);
			Long voteId = articleVotesRepo.findByPublisherEmailAndArticleId(publisherEmail, artId).get().getVoteId();
			
			if(vote.get().getSign() == likeOrDislike) {
				
				articleVotesRepo.deleteById(voteId);
				return 1;
				
			}else {
				
				articleVotesRepo.updatePublisherVote(likeOrDislike, voteId);
				return 3;
				
			}
			
			
			
		
		}//make your vote count 
		
		else {
			
			ArticleVotes vote = new ArticleVotes();
			vote.setArticleId(artId);
			vote.setPublisherEmail(publisherEmail);
			vote.setSign(likeOrDislike);
			articleVotesRepo.save(vote);
			
			return 2;
		}
	}

	

	private boolean checkPubliserHasVotedEarlierForThisArticle(String publisherEmail, Long artId) {
		
		Optional<ArticleVotes> articleVote = articleVotesRepo.checkSign(publisherEmail, artId);
		
		if(articleVote.isPresent()) {
			return true;
		}else {
			return false;
		}
	}
}
