package base.service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import base.dao.ArticleIndexRepo;
import base.dao.QuestionIndexRepo;
import base.model.Article;
import base.model.ArticleIndex;
import base.model.Question;
import base.model.QuestionIndex;

@Service
public class IndexService {

	@Autowired
	IndexService indexService;
	@Autowired
	QuestionIndexRepo questionIndexRepo;
	@Autowired
	ArticleIndexRepo articleIndexRepo;

	String[] auxilaryVerbs = { "i", "a", "in", "am", "of", "is", "are", "was", "and", "were", "being", "been", "be",
			"having", "have", "has", "had", "do", "does", "did", "will", "would", "shall", "should", "may", "might",
			"must", "can", "could", "they", "you", "we", "it", "to", "for", "the", "he", "she", "?", "how", "what",
			"why", "who", "when", "(", ")", "{", "}" };

//----------------------------------- Tokenization Operations ---------------------------------------------

//	a simple tokenizer method
	public Set<String> separateTags(String tags, String delimiter) {

		Set<String> tagsList = new HashSet<String>();
		StringTokenizer tokens = new StringTokenizer(tags.trim(), delimiter);
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken();
			tagsList.add(token);
		}
		return tagsList;
	}

//	tokenize as well as remove auxilary words
	public Set<String> tokenizeAndRemoveAuxilaryWords(String tags, String delimiter) {

		Set<String> tokenizedTags = indexService.separateTags(tags, delimiter);

		for (Iterator<String> iterator = tokenizedTags.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next().replace("_", " ");
			for (int i = 0; i < auxilaryVerbs.length; i++) {
				if (string.toLowerCase().equals(auxilaryVerbs[i])) {
					iterator.remove();
				}
			}
		}
		return tokenizedTags;
	}

//	tokenize the tags
	public Set<String> tokenizeTags(String tags, String delimiter) {

		Set<String> tokens = indexService.tokenizeAndRemoveAuxilaryWords(tags, delimiter);

		for (Iterator<String> iterator = tokens.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next().replace("_", " ");
			for (int i = 0; i < auxilaryVerbs.length; i++) {
				if (string.toLowerCase().equals(auxilaryVerbs[i])) {
					iterator.remove();
				}
			}
		}

		return tokens;
	}

//----------------------------------- Other Operations ---------------------------------------------

//	convert list<Long> 1,2,3.. to string 
	public String convertListToString(List<Long> listOfLong) {
		String str = listOfLong.stream().map(String::valueOf).collect(Collectors.joining(","));
		return str;
	}

//----------------------------------- Question Index Operations ---------------------------------------------

//	save Question's topic and tags to QuestionIndex
	public void saveQuestionTopicsAndTags(Question question) {

//		topic is been tokenized
		String topic = question.getTopic();

//		tags are been tokenized
		Set<String> tokenizedTags = indexService.tokenizeTags(question.getTags(), "[#]");

//		remove auxilary words 
		Set<String> tokenized = indexService.tokenizeAndRemoveAuxilaryWords(topic, " ");

		Long questionId = question.getqId();

//		save topics to QuestionIndex			
		for (Iterator<String> iterator = tokenized.iterator(); iterator.hasNext();) {
			String wordOfTopic = (String) iterator.next().toLowerCase();
			indexService.saveToQuestionIndex(questionId, wordOfTopic);
		}

//		save tags as well, to QuestionIndex
		for (Iterator<String> iterator = tokenizedTags.iterator(); iterator.hasNext();) {
			String tagOfTopic = (String) iterator.next().toLowerCase();
			indexService.saveToQuestionIndex(questionId, tagOfTopic);
		}
	}

//	save Or update -- QuestionIndex
	private void saveToQuestionIndex(Long questionId, String wordOfTopic) {
		Optional<QuestionIndex> word = questionIndexRepo.findByWord(wordOfTopic);
		if (word.isPresent()) {
			List<Long> presentIds = word.get().getQuestionIds();
			if (!presentIds.contains(questionId)) {
				presentIds.add(questionId); // add this question's id to the already present ids, 
											// if and only it is not present earlier
			}
			questionIndexRepo.updateQuestionIds(indexService.convertListToString(presentIds), wordOfTopic);
		} else {
			QuestionIndex questionIndex = new QuestionIndex();
			questionIndex.setWord(wordOfTopic);
			questionIndex.setQuestionIds(questionId.toString());
			questionIndexRepo.save(questionIndex);
		}
	}
//----------------------------------- Article Index Operations ---------------------------------------------

//	save Article's topic and tags to ArticleIndex
	public void saveArticleTopicsAndTags(Article article) {

//		topic is been tokenized
		String topic = article.getTopic();

//		tags are been tokenized
		Set<String> tokenizedTags = indexService.tokenizeTags(article.getTags(), "[#]");

//		remove auxilary words 
		Set<String> tokenized = indexService.tokenizeAndRemoveAuxilaryWords(topic, " ");

		Long articleId = article.getArticleId();

//		save topics to QuestionIndex			
		for (Iterator<String> iterator = tokenized.iterator(); iterator.hasNext();) {
			String wordOfTopic = (String) iterator.next().toLowerCase();
			indexService.saveToArticleIndex(articleId, wordOfTopic);
		}

//		save tags as well, to QuestionIndex
		for (Iterator<String> iterator = tokenizedTags.iterator(); iterator.hasNext();) {
			String tagOfTopic = (String) iterator.next().toLowerCase();
			indexService.saveToArticleIndex(articleId, tagOfTopic);
		}
	}

//	save Or update -- ArticleIndex
	private void saveToArticleIndex(Long articleId, String wordOfTopic) {
		Optional<ArticleIndex> word = articleIndexRepo.findByWord(wordOfTopic);
		if (word.isPresent()) {
			List<Long> presentIds = word.get().getArticleIds();
			if (!presentIds.contains(articleId)) {
				presentIds.add(articleId); // add this article's id to the already present ids, if and only it doesn't
										   // have that earlier
			}
			articleIndexRepo.updateArticleIds(indexService.convertListToString(presentIds), wordOfTopic);
		} else {
			ArticleIndex articleIndex = new ArticleIndex();
			articleIndex.setWord(wordOfTopic);
			articleIndex.setArticleIds(articleId.toString());
			articleIndexRepo.save(articleIndex);
		}
	}

}
