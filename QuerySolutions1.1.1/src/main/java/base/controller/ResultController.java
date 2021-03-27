package base.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import base.dao.AnswerRepo;
import base.dao.ArticleRepo;
import base.dao.QuestionRepo;
import base.model.Article;
import base.model.Question;
import base.service.IndexService;
import base.service.SearchService;

@RequestMapping("/view")
@RestController
public class ResultController {

	@Autowired
	QuestionRepo questionRepo;
	@Autowired
	AnswerRepo answerRepo;
	@Autowired
	ArticleRepo articleRepo;
	@Autowired
	SearchService searchService;
	@Autowired
	IndexService indexService;
	
	
	@RequestMapping(value = "/Question")
	public ModelAndView viewQuestion(@RequestParam("id") long id) {
		
		Question thisQuestion = questionRepo.findById(id).get();
		
		ModelAndView mv = new ModelAndView();
		mv.setViewName("viewQuestion");
		
		mv.addObject("question", thisQuestion);	//this question
		
		mv.addObject("tags", indexService.separateTags(thisQuestion.getTags(),"[#,@]"));	//the tags
		
		mv.addObject("allAnswer", answerRepo.findByQuestionId(id)); //all previous answers
		
		return mv;
	}

	@RequestMapping(value = "/Article")
	public ModelAndView viewArticle(@RequestParam("id") long id) {
		
		ModelAndView mv = new ModelAndView();
		mv.setViewName("viewArticle");
		Article article = articleRepo.findById(id).get();
		mv.addObject("article", article);	//this article
		mv.addObject("tags", indexService.separateTags(article.getTags(),"[#,@]")); //tags 
		return mv;
	}
	
	@RequestMapping(value = "/searchResult")
	public ModelAndView viewSearchResult(@RequestParam("search") String search) {
		Long start = System.currentTimeMillis();//starting time
		
		ModelAndView mv = searchService.searchOperation(search);
		
		String totalTime = "0."+Long.toString(System.currentTimeMillis() - start)+" seconds";
		//ending time
	
		mv.addObject("totalTime", totalTime);
		return mv;
	}
	
}
