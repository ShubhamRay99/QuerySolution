package base.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import base.dao.ArticleIndexRepo;
import base.dao.ArticleRepo;
import base.dao.QuestionIndexRepo;
import base.dao.QuestionRepo;
import base.model.Article;
import base.model.ArticleIndex;
import base.model.Question;
import base.model.QuestionIndex;

@Service
public class SearchService {

	@Autowired
	SearchService searchService;
	@Autowired
	QuestionRepo questionRepo;
	@Autowired
	ArticleRepo articleRepo;
	@Autowired
	IndexService indexService;
	@Autowired
	ArticleIndexRepo articleIndexRepo;
	@Autowired
	PublisherService publisherService;
	@Autowired
	QuestionIndexRepo questionIndexRepo;
	
//	List of recommended articles
	List<Article> recommendedArticles = new ArrayList<Article>();

//	List of recommended questions
	List<Question> recommendedQuestions = new ArrayList<Question>();

	public ModelAndView searchOperation(String search) {

		ModelAndView mv = new ModelAndView();
		mv.setViewName("searchResult");
		mv.addObject("searchedQuery", search);// searched Query for title purpose

//		convert to tokens and remove auxilary words
		Set<String> searchedTokens = indexService.tokenizeAndRemoveAuxilaryWords(search, " ");

//		find match from articles
		List<Article> articleList = new ArrayList<Article>(
				searchService.findFromArticles(search, searchedTokens).keySet());

//		find match from Questions
		List<Question> questionList = new ArrayList<Question>(
				searchService.findFromQuestions(search, searchedTokens).keySet());

		/*-----------------------------------display what you found---------------------------*/
		
//		display found articles
		mv.addObject("articleList", publisherService.modifyArticle(articleList));

//		display found questions
		mv.addObject("questionList", publisherService.modify(questionList));
		
//		Tutorial suggestion 
		mv.addObject("tutorial", searchService.tutorial(search));

//		display recommended Articles
		mv.addObject("recommendedArticles", recommendedArticles);
//		display recommended Questions
		mv.addObject("recommendedQuestions", recommendedQuestions);
		return mv;
	}

	
//	--------------------------------------------------- Question ------------------------------------------ //
//	find contents from Question table 
	@SuppressWarnings("unchecked")
	private HashMap<Question, Integer> findFromQuestions(String search, Set<String> searchedTokens) {

//		counterTable<ContentReference, No. of count>
		HashMap<Long, Integer> counterTable = new HashMap<Long, Integer>();

		for (Iterator iterator = searchedTokens.iterator(); iterator.hasNext();) {
			String searchedToken = (String) iterator.next().toString().toLowerCase();

//			retrieve matched token from Question Index
			Optional<QuestionIndex> matchedWordObject = questionIndexRepo.findByWord(searchedToken);

			if (matchedWordObject.isPresent()) {
				/*
				 * a list of ids are attached to the 'matchedWordObject'. Now, they are
				 * separated and stored in a counterTable.
				 */
				List<Long> matchedIds = matchedWordObject.get().getQuestionIds();

				for (Iterator iterator2 = matchedIds.iterator(); iterator2.hasNext();) {
					Long id = (Long) iterator2.next();

//					add the Id, if the counter table don't have that yet..
					counterTable.putIfAbsent(id, 1);

//					and increment the counter, if the counter table have it earlier
					counterTable.computeIfPresent(id, (reference, counter) -> counter + 1);

				}
			}
		}

//		get perfectly matchedContents 	and sort those in descending order
		HashMap<Question, Integer> matchedQuestion = (HashMap<Question, Integer>) searchService
				.sortByValue(searchService.getPerfectionInQuestion(counterTable, search));

		return matchedQuestion;
	}
	
//	final check in question table
	private HashMap<Question, Integer> getPerfectionInQuestion(HashMap<Long, Integer> counterTable, String rawSearch) {
		
		HashMap<Question, Integer> matchedQuestion = new HashMap<Question, Integer>();

		Set<Long> questionIds = counterTable.keySet();
		int i = 0;

//		make -- simple tokens of rawSearch
		List<String> rawTokens = Arrays.asList(rawSearch.toLowerCase().split(" "));
		int quotient = 100 / rawTokens.size();

//		make recommended Questions as empty before adding new content to those.
		recommendedQuestions.clear();
		
//		iterate through the question dB display only 5 contents
		for (Iterator iterator = questionIds.iterator(); (iterator.hasNext()) && (i < 5); i++) {
			Long id = (Long) iterator.next();

			Optional<Question> question = questionRepo.findById(id);
			int matchPercent = 0;
			
			if(question.isPresent()) {
				
//			visit article topic to find a perfect match
			List<String> topic = Arrays.asList(question.get().getTopic().toLowerCase().split(" "));

//			Iterate over the topic and check which word of search token get matches with this 
			for (Iterator iterator2 = topic.iterator(); iterator2.hasNext();) {
				String eachTopic = (String) iterator2.next();

				if (rawTokens.contains(eachTopic)) {
					matchPercent += quotient;
				}

			}

//			priority chart
			if (matchPercent >= 95) {

				matchedQuestion.put(question.get(), 10);

			} else if ((matchPercent < 95) && (matchPercent >= 85)) {

				matchedQuestion.put(question.get(), 9);

			} else if ((matchPercent < 85) && (matchPercent >= 65)) {

				matchedQuestion.put(question.get(), 8);

			} else {

				recommendedQuestions.add(question.get());

			}

			if (i > counterTable.size() - 1) { // when value of i exceedes more than its size
				break;
			}
			}	
		}

		return matchedQuestion;
	}

//	--------------------------------------------------- Article ------------------------------------------ //
//	find contents from article table 
	@SuppressWarnings("unchecked")
	private HashMap<Article, Integer> findFromArticles(String search, Set<String> searchedTokens) {

//		counterTable<ContentReference, No. of count>
		HashMap<Long, Integer> counterTable = new HashMap<Long, Integer>();

		for (Iterator iterator = searchedTokens.iterator(); iterator.hasNext();) {
			String searchedToken = (String) iterator.next().toString().toLowerCase();

//			retrieve matched token from Article Index
			Optional<ArticleIndex> matchedWordObject = articleIndexRepo.findByWord(searchedToken);

			if (matchedWordObject.isPresent()) {
				/*
				 * a list of ids are attached to the 'matchedWordObject'. Now, they are
				 * separated and stored in a counterTable.
				 */
				List<Long> matchedIds = matchedWordObject.get().getArticleIds();

				for (Iterator iterator2 = matchedIds.iterator(); iterator2.hasNext();) {
					Long id = (Long) iterator2.next();

//					add the Id, if the counter table don't have that yet..
					counterTable.putIfAbsent(id, 1);

//					and increment the counter, if the counter table have it earlier
					counterTable.computeIfPresent(id, (reference, counter) -> counter + 1);

				}
			}
		}

//		get perfectly matchedContents 	and sort those in descending order
		HashMap<Article, Integer> matchedArticles = (HashMap<Article, Integer>) searchService
				.sortByValue(searchService.getPerfectionInArticles(counterTable, search));

		return matchedArticles;
	}

//	final check in article table
	private HashMap<Article, Integer> getPerfectionInArticles(HashMap<Long, Integer> counterTable, String rawSearch) {

		HashMap<Article, Integer> matchedArticles = new HashMap<Article, Integer>();

		Set<Long> articleIds = counterTable.keySet();
		int i = 0;

//		make -- simple tokens of rawSearch
		List<String> rawTokens = Arrays.asList(rawSearch.toLowerCase().split(" "));
		int quotient = 100 / rawTokens.size();

//		make the recommeded article list as empty before adding any new content to it.
		recommendedArticles.clear();
		
//		iterate through the article dB display only 5 contents
		for (Iterator iterator = articleIds.iterator(); (iterator.hasNext()) && (i < 5); i++) {
			Long id = (Long) iterator.next();

			Optional<Article> article = articleRepo.findById(id);
			int matchPercent = 0;
			
			if(article.isPresent()) {
//			visit article topic to find a perfect match
			List<String> topic = Arrays.asList(article.get().getTopic().toLowerCase().split(" "));

//			Iterate over the topic and check which word of search token get matches with this 
			for (Iterator iterator2 = topic.iterator(); iterator2.hasNext();) {
				String eachTopic = (String) iterator2.next();

				if (rawTokens.contains(eachTopic)) {
					matchPercent += quotient;
				}

			}

//			priority chart
			if (matchPercent >= 95) {

				matchedArticles.put(article.get(), 10);

			} else if ((matchPercent < 95) && (matchPercent >= 85)) {

				matchedArticles.put(article.get(), 9);

			} else if ((matchPercent < 85) && (matchPercent >= 65)) {

				matchedArticles.put(article.get(), 8);

			} else {

//				add them as recommended articles				
				recommendedArticles.add(article.get());
				
			}

			if (i > counterTable.size() - 1) { // when value of i exceedes more than its size
				break;
			}
		}
		}

		return matchedArticles;
	}

// function to sort hashmap by values - descending order -- universal function
	public HashMap<?, Integer> sortByValue(HashMap<?, Integer> hm) {

		// Create a list from elements of HashMap
		List<Map.Entry<?, Integer>> list = new LinkedList<Map.Entry<?, Integer>>(hm.entrySet());

		// Sort the list
		Collections.sort(list, new Comparator<Map.Entry<?, Integer>>() {
			public int compare(Map.Entry<?, Integer> o1, Map.Entry<?, Integer> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// put data from sorted list to hashmap
		HashMap<Object, Integer> temp = new LinkedHashMap<Object, Integer>();
		for (Entry<?, Integer> aa : list) {
			temp.put(aa.getKey(), aa.getValue());
		}

		return temp;
	}

//	tutorial's help
	public String tutorial(String searchQuery) {

		if ((searchQuery.contains("java")) && !(searchQuery.contains("javascript"))) {

			if (searchQuery.contains("conditional operators") || searchQuery.contains("decision making")
					|| searchQuery.contains("if else"))
				return "java";
			else if (searchQuery.contains("object") || searchQuery.contains("object class")) {
				return "objectClass_java";
			} else {
				return "java";
			}

		} else if (searchQuery.contains("python")) {

			if (searchQuery.contains("dictionary")) {
				return "python";
			} else if (searchQuery.contains("json")) {
				return "python_json";
			} else if (searchQuery.contains("exception") || searchQuery.contains("handling")) {
				return "python_exception";
			} else {
				return "python";
			}
		} else if (searchQuery.contains("c language")) {

			if (searchQuery.contains("pointers") || searchQuery.contains("structure")) {
				return "c";
			}
			return "c";
		} else if (searchQuery.contains("sql")) {

			if (searchQuery.contains("syntax") || searchQuery.contains("operations")) {
				return "sql";
			}
			return "sql";
		} else if (searchQuery.contains("cpp") || searchQuery.contains("c++")) {

			if (searchQuery.contains("friend function") || searchQuery.contains("string")) {
				return "cpp";
			}
			return "cpp";
		} else if (searchQuery.contains("angular")) {

			if (searchQuery.contains("modules")) {
				return "arngular";
			} else if (searchQuery.contains("directives")) {
				return "directives_angular";
			} else {
				return "angular";
			}
		}

		return null;
	}

}
