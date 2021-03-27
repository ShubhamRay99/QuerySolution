package base.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import base.dao.AnswerRepo;
import base.dao.ArticleRepo;
import base.dao.PublisherRepo;
import base.dao.QuestionRepo;
import base.model.Answers;
import base.model.Article;
import base.model.Question;
import base.service.AnswerVoteService;
import base.service.ArticleVoteService;
import base.service.IndexService;
import base.service.PublisherService;
import base.service.SearchService;

@RestController
@RequestMapping("/publisher")
public class PublisherController {

	@Autowired
	QuestionRepo questionRepo;
	@Autowired
	PublisherService publisherService;
	@Autowired
	PublisherRepo publisherRepo;
	@Autowired
	AnswerRepo answerRepo;
	@Autowired
	AnswerVoteService voteService;
	@Autowired
	ArticleRepo articleRepo;
	@Autowired
	ArticleVoteService articleVoteService;
	@Autowired
	SearchService searchService;
	@Autowired
	IndexService indexService;

	@RequestMapping(value = { "/welcome", "/welcome/{QuestionPageNumber}/{ArticlePageNumber}" })
	public ModelAndView page(Authentication authentication, @PathVariable Optional<Integer> QuestionPageNumber,
			@PathVariable Optional<Integer> ArticlePageNumber) {

		ModelAndView mv = publisherService.userPageContents(authentication, QuestionPageNumber, ArticlePageNumber);

		return mv;
	}

	/* save the Question */
	@PostMapping("/saveQuestion")
	public ModelAndView saveQuestion(@ModelAttribute Question question, Authentication authentication, HttpServletResponse response) {
//		attach the user name and save the question
		question.setPublisherEmail(authentication.getName());
		Question savedQuestion = questionRepo.save(question);

//		save the Question in QuestionTopicIndex
		indexService.saveQuestionTopicsAndTags(savedQuestion);

		response.setHeader("Cache-Control", "no-store, max-age=0, must-revalidate");
		
		Optional<Integer> questionPageNumber = Optional.ofNullable(null); // nothing to done here
		Optional<Integer> articlepageNumber = Optional.ofNullable(null); // nothing to done here
		ModelAndView mv = publisherService.userPageContents(authentication, questionPageNumber, articlepageNumber);
		return mv;
	}

	/* save the answer */
	@RequestMapping(value = "/saveAnswer")
	public ModelAndView viewQuestion(@RequestParam("id") long id, @ModelAttribute Answers answer,
			Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
		// here the id is the questionId
		answer.setPublisherEmail(authentication.getName());
		answer.setQuestionId(id);
		answerRepo.save(answer);

		ModelAndView mv = new ModelAndView();
		Question thisQuestion = questionRepo.findById(id).get();
		mv.addObject("allAnswer", answerRepo.findByQuestionId(id)); // all previous answers
		mv.addObject("question", thisQuestion); // this Question

		// the tags associated with question
		Set<String> tags = indexService.separateTags(thisQuestion.getTags(), "[#,@]");
		mv.addObject("tags", tags);

		response.setHeader("Cache-Control", "no-store, max-age=0, must-revalidate");
		
		mv.setViewName("viewQuestion");

		/*
		 * mail the users -> who have asked that question, who have voted for that
		 * answer earlier, who have previously given the answer to that question
		 */
		publisherService.mailTheSpectators(thisQuestion, authentication.getName(), request);

		return mv;
	}

	/* like the answer */
	@ResponseBody
	@PostMapping("/like-this-answer/{queId}/{ansId}")
	public ResponseEntity<?> likeAnswer(Authentication authentication, @PathVariable("queId") Long queId,
			@PathVariable("ansId") Long ansId) {
		int voteDecision = voteService.makeVoteCount(authentication.getName(), ansId, queId, 1);// 1 means the publisher
																								// liked the content

		/*
		 * makeVoteCount will be returning 3 values 0 -> if publisher isn't eligble to
		 * vote 1 -> if publisher has already given the vote as like - then the vote
		 * will be disabled 2 -> if publisher is voting for the first time. 3 -> if
		 * publisher changes his vote.
		 */
		try {
			return ResponseEntity.status(200).body(voteDecision);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(4);
		}
	}

	/* dislike the answer */
	@ResponseBody
	@PostMapping("/dislike-this-answer/{queId}/{ansId}")
	public int dislikeAnswer(Authentication authentication, @PathVariable("queId") Long queId,
			@PathVariable("ansId") Long ansId) {
		int voteDecision = voteService.makeVoteCount(authentication.getName(), ansId, queId, 0);// 0 means the publisher
																								// disliked the content

		/*
		 * makeVoteCount will be returning 3 values 0 -> if publisher isn't eligble to
		 * vote 1 -> if publisher has already given the vote as like - then the vote
		 * will be disabled 2 -> if publisher is voting for the first time.
		 */
		return voteDecision;
	}

	/* save the Article */
	@RequestMapping("/saveArticle")
	public ModelAndView saveArticle(@ModelAttribute Article article, Authentication authentication, HttpServletResponse response) {

		article.setPublisherEmail(authentication.getName());
		Article savedArticle = articleRepo.save(article);

//		save articles topics and tags in the indexes class
		indexService.saveArticleTopicsAndTags(savedArticle);
		
		response.setHeader("Cache-Control", "no-store, max-age=0, must-revalidate");
		
		Optional<Integer> recommendedpageNumber = Optional.ofNullable(null); // nothing to done here
		Optional<Integer> ownpageNumber = Optional.ofNullable(null); // nothing to done here
		ModelAndView mv = publisherService.userPageContents(authentication, recommendedpageNumber, ownpageNumber);
		return mv;
	}

	/* like the article */
	@ResponseBody
	@PostMapping("/like-this-article/{artId}")
	public ResponseEntity<?> likeArticle(Authentication authentication, @PathVariable("artId") Long artId) {
		int voteDecision = articleVoteService.makeVoteCountForArticle(authentication.getName(), artId, 1);// 1 means the
																											// publisher
																											// liked the
																											// content

		/*
		 * makeVoteCount will be returning 3 values 0 -> if publisher isn't eligble to
		 * vote 1 -> if publisher has already given the vote as like - then the vote
		 * will be disabled 2 -> if publisher is voting for the first time. 3 -> if
		 * publisher changes his vote.
		 */
		return ResponseEntity.status(200).body(voteDecision);
	}

	/* dislike the article */
	@ResponseBody
	@PostMapping("/dislike-this-article/{artId}")
	public ResponseEntity<?> dislikeArticle(Authentication authentication, @PathVariable("artId") Long artId) {
		int voteDecision = articleVoteService.makeVoteCountForArticle(authentication.getName(), artId, 0);// 0 means the
																											// publisher
																											// disliked
																											// the
																											// content

		/*
		 * makeVoteCount will be returning 3 values 0 -> if publisher isn't eligble to
		 * vote 1 -> if publisher has already given the vote as like - then the vote
		 * will be disabled 2 -> if publisher is voting for the first time. 3 -> if
		 * publisher changes his vote.
		 */
		return ResponseEntity.status(200).body(voteDecision);
	}

	/* profile photo upload */
	@PostMapping("/uploadProfilePic")
	public ModelAndView uploadProfilePic(@RequestParam("profilePic") MultipartFile profilePic,
			Authentication authentication, HttpServletResponse response) {
		ModelAndView mv = new ModelAndView();

		
		File file = new File(profilePic.getOriginalFilename());

		String mimetype = null;
		try {
			mimetype = Files.probeContentType(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}

//		checking it is a image file or not
		if (mimetype != null && mimetype.split("/")[0].equals("image")) {

			try {

				Path currentPath = Paths.get(".");
				Path absolutePath = currentPath.toAbsolutePath();

				// save the photo name in dB
				int returned = publisherRepo.UpdateProfilePic(profilePic.getOriginalFilename(),
						authentication.getName());

				byte[] bytes = profilePic.getBytes();

				// save the photo actually in this location
				Path path = Paths.get(
						absolutePath + "/src/main/resources/static/profilePhoto/" + profilePic.getOriginalFilename());
				Files.write(path, bytes);

				// if photo saved successfully
				if (returned != 0) {
//					mv.addObject("saved", "Photo Saved");
//					mv.addObject("image", Base64.getMimeEncoder().encodeToString(profilePic.getBytes()));
					
					response.setHeader("Cache-Control", "no-store, max-age=0, must-revalidate");
					
					Optional<Integer> questionPageNumber = Optional.ofNullable(null); // nothing to done here
					Optional<Integer> articlepageNumber = Optional.ofNullable(null); // nothing to done here
					mv = publisherService.userPageContents(authentication, questionPageNumber, articlepageNumber);
					return mv;
				}
			} catch (Exception e) {
				mv.addObject("error", "Error Occured While Saving the photo" + e);
			}

		} else {
			mv.addObject("notImage", profilePic.getOriginalFilename() + " is not an image file");
		}

		mv.setViewName("temp");
		return mv;
	}
}
