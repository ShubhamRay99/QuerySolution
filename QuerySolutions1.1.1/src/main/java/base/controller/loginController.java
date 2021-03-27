package base.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import base.dao.ArticleRepo;
import base.dao.PublisherRepo;
import base.dao.QuestionRepo;
import base.model.Article;
import base.model.Publisher;
import base.model.Question;
import base.service.PublisherService;

@RestController
public class loginController {

	@Autowired
	QuestionRepo questionRepo;

	@Autowired
	PublisherService publisherService;

	@Autowired
	ArticleRepo articleRepo;

	@Autowired
	PublisherRepo publisherRepo;

//	handling home page
	@RequestMapping(value = { "/", "/page/{QuesPageNumber}/{ArticlePageNumber}" })
	public ModelAndView welcome(@PathVariable Optional<Integer> QuesPageNumber,
			@PathVariable Optional<Integer> ArticlePageNumber) {
		ModelAndView modelandview = new ModelAndView();
		modelandview.setViewName("home");

//		for questions
		List<Question> questions = publisherService.allQuestions(QuesPageNumber);

		modelandview.addObject("questions", questions);
//		get current page and all pages size for pagination
		List<Question> allQuestions = questionRepo.findAll();

		int size = allQuestions.size();
		int pageSize = size / 5;// I want to display 5 contents in a single page
		if (size % 5 != 0) {
			pageSize += 1; // the remaining pages are added here
		}

		modelandview.addObject("ques_pageSize", pageSize);
		if (QuesPageNumber.isPresent()) {
			modelandview.addObject("QuesCurrentPage", QuesPageNumber.get().intValue());
		} else {
			modelandview.addObject("QuesCurrentPage", 0);
		}
//		for articles
		List<Article> articles = publisherService.allArticles(ArticlePageNumber);

		modelandview.addObject("articles", articles);

//		get current page and all pages size for pagination
		List<Article> allArticles = articleRepo.findAll();

		int artSize = allArticles.size();
		int artPageSize = artSize / 5;// I want to display 5 contents in a single page
		if (artSize % 5 != 0) {
			artPageSize += 1; // the remaining pages are added here
		}

		modelandview.addObject("art_pageSize", artPageSize);
		if (ArticlePageNumber.isPresent()) {
			modelandview.addObject("ArtCurrentPage", ArticlePageNumber.get().intValue());
		} else {
			modelandview.addObject("ArtCurrentPage", 0);
		}

		return modelandview;
	}

	@RequestMapping("/course")
	public ModelAndView course() {
		ModelAndView modelandview = new ModelAndView();
		modelandview.setViewName("course");
		return modelandview;
	}

//---------------------------- admin pages-----------------------

	@RequestMapping("/adminLogin")
	public ModelAndView secureAdmin() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("adminLogin");
		return mv;
	}

	@RequestMapping("/admin/dashboard")
	public ModelAndView welcomeAdmin() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("dashboard");
		return mv;
	}

//	--------------------------- user pages-----------------------

	@RequestMapping("/login")
	public ModelAndView secureUser(@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "logout", required = false) String logout) {

		ModelAndView model = new ModelAndView();
		if (error != null) {
			model.addObject("error", "Invalid username and password!");
		}

		if (logout != null) {
			model.addObject("logout", "You've been logged out successfully.");
		}
		model.setViewName("login");

		return model;
	}

	@RequestMapping("/registrationPage")
	public ModelAndView registrationPage() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("registrationPage");
		return mv;
	}

//	--------------------------- Publisher Registration-----------------------

	@RequestMapping("/register")
	public ModelAndView register(@ModelAttribute Publisher publisher, BindingResult bindingResult,
			HttpServletRequest request) {
		ModelAndView mv = new ModelAndView();
		String uniqueId = UUID.randomUUID().toString();

		if (!publisher.getConfirmPassword().equals(publisher.getPassword())) {

			bindingResult.reject("password");
			mv.addObject("passwordNotSame", "Oops!  The password and confirm password are not same.");
			mv.setViewName("registrationPage");

		} // validate the email
		else if (publisherRepo.findByUsername(publisher.getUsername()) != null) {

			bindingResult.reject("email");
			mv.addObject("alreadyRegisteredMessage",
					"Oops!  There is already a user registered with the email provided.");
			mv.setViewName("registrationPage");
 
		} else {

			// save publisher details.
			publisher.setUniqueId(uniqueId);
			publisher.setPassword(encoder().encode(publisher.getPassword()));

			publisherRepo.save(publisher);

			publisherService.mailToValidate(publisher, request);

			mv.addObject("confirmationMessage", "A confirmation email has been sent to " + publisher.getUsername());
			mv.setViewName("login");

		}

		return mv;
	}

	private BCryptPasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}

	@RequestMapping("/validateAccount")
	public ModelAndView register(ModelAndView modelAndView, @RequestParam("uniqueId") String uniqueId,
			@RequestParam("publisherId") Long publisherId) {

		// link verification
		modelAndView = publisherService.verifyLink(uniqueId, publisherId);

		return modelAndView;
	}

}
