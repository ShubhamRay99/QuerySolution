package base.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/static")
public class StaticFilesController {
	
//	-----------------------------------------------------------------view the Question and article page
		@GetMapping("/Question")
		public ModelAndView askAQuestion() {
			ModelAndView mv = new ModelAndView();
			mv.setViewName("Question");
			return mv;
		}
		
		@GetMapping("/Article")
		public ModelAndView article() {
			ModelAndView mv = new ModelAndView();
			mv.setViewName("article");
			return mv;
		}

//		-----------------------------------------------------------------java
	@RequestMapping("/java")
	public ModelAndView java() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("java");
		return mv;
	}
	@RequestMapping("/objectClass_java")
	public ModelAndView objectclass_java() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("objectclass_java");
		return mv;
	}
//	-----------------------------------------------------------------angular
	@RequestMapping("/directives_angular")
	public ModelAndView directives_angular() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("directives_angular");
		return mv;
	}

	@RequestMapping("/angular")
	public ModelAndView angular() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("angular");
		return mv;
	}
//	-----------------------------------------------------------------pyhton
	@RequestMapping("/python")
	public ModelAndView python() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("python");
		return mv;
	}

	@RequestMapping("/python_json")
	public ModelAndView python_json() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("python_json");
		return mv;
	}

	@RequestMapping("/python_exception")
	public ModelAndView python_exception() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("python_exception");
		return mv;
	}
	
//	--------------------------------------------------------sql
	@RequestMapping("/sql")
	public ModelAndView sql() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("sql");
		return mv;
	}

	@RequestMapping("/operation_sql")
	public ModelAndView operation_sql() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("operation_sql");
		return mv;
	}

//	--------------------------------------------------------c
	
	@RequestMapping("/c")
	public ModelAndView c() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("c");
		return mv;
	}
	
//	--------------------------------------------------------cpp
	
	@RequestMapping("/cpp")
	public ModelAndView cpp() {
		ModelAndView mv = new ModelAndView();
		mv.setViewName("cpp");
		return mv;
	}
	
}

