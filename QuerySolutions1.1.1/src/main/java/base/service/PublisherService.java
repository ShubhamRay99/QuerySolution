package base.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import base.dao.AnswerRepo;
import base.dao.AnswerVotesRepo;
import base.dao.ArticleRepo;
import base.dao.ArticleVotesRepo;
import base.dao.AuthoritiesRepo;
import base.dao.PublisherRepo;
import base.dao.QuestionRepo;
import base.model.AnswerVotes;
import base.model.Answers;
import base.model.Article;
import base.model.Authorities;
import base.model.Publisher;
import base.model.Question;

@Service
public class PublisherService {

	@Autowired
	PublisherService publisherService;
	@Autowired
	PublisherRepo publisherRepo;
	@Autowired
	AuthoritiesRepo authoritiesRepo;
	@Autowired
	QuestionRepo questionRepo;
	@Autowired
	ArticleRepo articleRepo;
	@Autowired
	private JavaMailSender sender;
	@Autowired
	AnswerRepo answerRepo;
	@Autowired
	AnswerVotesRepo answerVotesRepo;
	@Autowired
	ArticleVotesRepo articleVotesRepo;

	public List<Question> modify(List<Question> contents) {

		List<Question> modifiedList = new ArrayList<Question>();

		for (int i = 0; i < contents.size(); i++) {

			String description = contents.get(i).getDescription();
			if (description.length() > 200) {
				description = description.substring(0, 200) + "...";
			}

			modifiedList.add(new Question(contents.get(i).getqId(), contents.get(i).getTopic(), description,
					contents.get(i).getTags(), contents.get(i).getPublisherEmail(), contents.get(i).getCreatedOn()));

		}

		return modifiedList;
	}

	public List<Article> modifyArticle(List<Article> contentsOfUser) {

		List<Article> modifiedList = new ArrayList<Article>();

		for (int i = 0; i < contentsOfUser.size(); i++) {

			String description = contentsOfUser.get(i).getContent();
			if (description.length() > 200) {
				description = description.substring(0, 200) + "...";
			}

			modifiedList.add(new Article(contentsOfUser.get(i).getArticleId(), contentsOfUser.get(i).getTopic(),
					description, contentsOfUser.get(i).getTags(), contentsOfUser.get(i).getPublisherEmail(),
					contentsOfUser.get(i).getCreatedOn()));

		}

		return modifiedList;
	}

	public ModelAndView userPageContents(Authentication authentication, Optional<Integer> QuestionPageNumber,
			Optional<Integer> ArticlePageNumber) {

		// return the userIntro page
		ModelAndView mv = new ModelAndView();
		mv.setViewName("userIntro");

//		publisher's own Asked Questions ********************************************************

		// pagination for user written questions
		PageRequest pageReq;
		Sort sort = Sort.by("createdOn").descending();

		if (QuestionPageNumber.isPresent()) {
			pageReq = PageRequest.of(QuestionPageNumber.get().intValue(), 2, sort);
		} else {
			pageReq = PageRequest.of(0, 2, sort);
		}

		List<Question> modifiedUsersquestions = publisherService
				.modify(questionRepo.findContentsOfUserOnPage(authentication.getName(), pageReq).toList());

		mv.addObject("questions", modifiedUsersquestions);

//		get current page and all pages size for pagination-- publisher Own content
		List<Question> allQuestions = questionRepo.findContentsOfUser(authentication.getName());

		int size = allQuestions.size();
		int pageSize = size / 2;// I want to display 2 contents in a single page

		if (size % 2 != 0) {
			pageSize += 1; // the remaining pages are added here
		}

		mv.addObject("quePageSize", pageSize);
		if (QuestionPageNumber.isPresent()) {
			mv.addObject("queCurrentPage", QuestionPageNumber.get().intValue());
		} else {
			mv.addObject("queCurrentPage", 0);
		}

//		publisher's own Written Articles ******************************************

		// pagination for user written articles
		PageRequest pageReqArt;
		Sort sortArt = Sort.by("createdOn").descending();

		if (ArticlePageNumber.isPresent()) {
			pageReqArt = PageRequest.of(ArticlePageNumber.get().intValue(), 2, sortArt);
		} else {
			pageReqArt = PageRequest.of(0, 2, sortArt);
		}
		// pagination for user written content

		List<Article> modifiedArticles = publisherService
				.modifyArticle(articleRepo.findArticleOfUserOnPage(authentication.getName(), pageReqArt).toList());

		mv.addObject("articles", modifiedArticles);


//		get current page and all pages size for pagination-- publisher Own content
		List<Article> allArticles = articleRepo.findByPublisherEmail(authentication.getName());

		int artSize = allArticles.size();
		int artPageSize = artSize / 2;// I want to display 2 contents in a single page

		if (artSize % 2 != 0) {
			artPageSize += 1; // the remaining pages are added here
		}

		mv.addObject("artPageSize", artPageSize);
		if (ArticlePageNumber.isPresent()) {
			mv.addObject("artCurrentPage", ArticlePageNumber.get().intValue());
		} else {
			mv.addObject("artCurrentPage", 0);
		}

//		publisher profile
		Publisher publisher = publisherRepo.findByUsername(authentication.getName());
//		mv.addObject("src", "/img/mike.jpg");
		mv.addObject("name", publisher.getName());
		
//		user asked no. of question
		mv.addObject("contributedQuestion", size);
//		user asked no. of Articles
		mv.addObject("contributedArticle", artSize);
		//		got total Likes
		mv.addObject("TotaLikes", answerVotesRepo.findByPublisherEmailOfLike(authentication.getName()).size()+
				articleVotesRepo.findByPublisherEmailOfLike(authentication.getName()).size());
//		got total disLikes
		mv.addObject("TotaDisLikes", answerVotesRepo.findByPublisherEmailOfdisLike(authentication.getName()).size()+
				articleVotesRepo.findByPublisherEmailOfdisLike(authentication.getName()).size());
		
		
		
//		user's profile photo

		String image = (publisher.getProfilePic() == null)?null: publisher.getProfilePic();
		
		mv.addObject("image",image);
		
		
		
		return mv;
	}

	public List<Question> allQuestions(Optional<Integer> pageNumber) {

		// pagination
		PageRequest pageReq;
		Sort sort = Sort.by("createdOn").descending();

		if (pageNumber.isPresent()) {
			pageReq = PageRequest.of(pageNumber.get().intValue(), 5, sort);
		} else {
			pageReq = PageRequest.of(0, 5, sort);
		}
		Page<Question> page = questionRepo.findAll(pageReq);

		// convert Page to List
		List<Question> questions = publisherService.modify(page.toList());

		return questions;
	}

	public List<Article> allArticles(Optional<Integer> artilcePageNumber) {
		// pagination
		PageRequest pageReq;
		Sort sort = Sort.by("createdOn").descending();

		if (artilcePageNumber.isPresent()) {
			pageReq = PageRequest.of(artilcePageNumber.get().intValue(), 5, sort);
		} else {
			pageReq = PageRequest.of(0, 5, sort);
		}
		Page<Article> page = articleRepo.findAll(pageReq);

		// convert Page to List
		List<Article> articles = publisherService.modifyArticle(page.toList());

		return articles;
	}

	/*
	 * mail the users -> who have asked that question, who have voted for that
	 * answer earlier, who have previously given the answer to that question
	 */
	public void mailTheSpectators(Question thisQuestion, String answeredNow, HttpServletRequest request) {
		MimeMessage msg = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(msg);

		List<String> spectators = new ArrayList<String>();

//		The publisher who asked that question 
		spectators.add(thisQuestion.getPublisherEmail());

//		The publisher those who have answered that question, and 
//		the situation can be like question haven't been answered yet
		List<Answers> answers = answerRepo.findByQuestionId(thisQuestion.getqId());

//		remember the list can be empty too
		if (answers != null) {
			for (Iterator iterator = answers.iterator(); iterator.hasNext();) {
				Answers answer = (Answers) iterator.next();
				spectators.add(answer.getPublisherEmail());

//				find the votes of the publisher 
				List<AnswerVotes> votes = answerVotesRepo.findAllByAnswerId(answer.getId());
//				the list can be empty too
				if (votes != null) {
					for (Iterator iterator2 = votes.iterator(); iterator2.hasNext();) {
						AnswerVotes vote = (AnswerVotes) iterator2.next();
						spectators.add(vote.getPublisherEmail());
					}
				}
			}
		}

		try {
//			send mail.. to the respective publishers
			String link = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
					+ request.getContextPath() + "/view/Question?id=" + thisQuestion.getqId();
			for (Iterator iterator = spectators.iterator(); iterator.hasNext();) {
				String publisherEmail = (String) iterator.next();
				helper.setTo(publisherEmail);
				helper.setSubject("A new answer found to your question, have a look..");
				helper.setText(
						"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\r\n"
								+ "\r\n" + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\r\n" + "<head>\r\n"
								+ "	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\r\n"
								+ "	<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\r\n"
								+ "	<title>[SUBJECT]</title>\r\n" + "	<style type=\"text/css\">\r\n" + "\r\n"
								+ "@media screen and (max-width: 600px) {\r\n" + "    table[class=\"container\"] {\r\n"
								+ "        width: 95% !important;\r\n" + "    }\r\n" + "}\r\n" + "\r\n"
								+ "	#outlook a {padding:0;}\r\n"
								+ "		body{width:100% !important; -webkit-text-size-adjust:100%; -ms-text-size-adjust:100%; margin:0; padding:0;}\r\n"
								+ "		.ExternalClass {width:100%;}\r\n"
								+ "		.ExternalClass, .ExternalClass p, .ExternalClass span, .ExternalClass font, .ExternalClass td, .ExternalClass div {line-height: 100%;}\r\n"
								+ "		#backgroundTable {margin:0; padding:0; width:100% !important; line-height: 100% !important;}\r\n"
								+ "		img {outline:none; text-decoration:none; -ms-interpolation-mode: bicubic;}\r\n"
								+ "		a img {border:none;}\r\n" + "		.image_fix {display:block;}\r\n"
								+ "		p {margin: 1em 0;}\r\n"
								+ "		h1, h2, h3, h4, h5, h6 {color: black !important;}\r\n" + "\r\n"
								+ "		h1 a, h2 a, h3 a, h4 a, h5 a, h6 a {color: blue !important;}\r\n" + "\r\n"
								+ "		h1 a:active, h2 a:active,  h3 a:active, h4 a:active, h5 a:active, h6 a:active {\r\n"
								+ "			color: red !important; \r\n" + "		 }\r\n" + "\r\n"
								+ "		h1 a:visited, h2 a:visited,  h3 a:visited, h4 a:visited, h5 a:visited, h6 a:visited {\r\n"
								+ "			color: purple !important; \r\n" + "		}\r\n" + "\r\n"
								+ "		table td {border-collapse: collapse;}\r\n" + "\r\n"
								+ "		table { border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt; }\r\n"
								+ "\r\n" + "		a {color: #000;}\r\n" + "\r\n"
								+ "		@media only screen and (max-device-width: 480px) {\r\n" + "\r\n"
								+ "			a[href^=\"tel\"], a[href^=\"sms\"] {\r\n"
								+ "						text-decoration: none;\r\n"
								+ "						color: black; /* or whatever your want */\r\n"
								+ "						pointer-events: none;\r\n"
								+ "						cursor: default;\r\n" + "					}\r\n" + "\r\n"
								+ "			.mobile_link a[href^=\"tel\"], .mobile_link a[href^=\"sms\"] {\r\n"
								+ "						text-decoration: default;\r\n"
								+ "						color: orange !important; /* or whatever your want */\r\n"
								+ "						pointer-events: auto;\r\n"
								+ "						cursor: default;\r\n" + "					}\r\n"
								+ "		}\r\n" + "\r\n" + "\r\n"
								+ "		@media only screen and (min-device-width: 768px) and (max-device-width: 1024px) {\r\n"
								+ "			a[href^=\"tel\"], a[href^=\"sms\"] {\r\n"
								+ "						text-decoration: none;\r\n"
								+ "						color: blue; /* or whatever your want */\r\n"
								+ "						pointer-events: none;\r\n"
								+ "						cursor: default;\r\n" + "					}\r\n" + "\r\n"
								+ "			.mobile_link a[href^=\"tel\"], .mobile_link a[href^=\"sms\"] {\r\n"
								+ "						text-decoration: default;\r\n"
								+ "						color: orange !important;\r\n"
								+ "						pointer-events: auto;\r\n"
								+ "						cursor: default;\r\n" + "					}\r\n"
								+ "		}\r\n" + "\r\n"
								+ "		@media only screen and (-webkit-min-device-pixel-ratio: 2) {\r\n"
								+ "			/* Put your iPhone 4g styles in here */\r\n" + "		}\r\n" + "\r\n"
								+ "		@media only screen and (-webkit-device-pixel-ratio:.75){\r\n"
								+ "			/* Put CSS for low density (ldpi) Android layouts in here */\r\n"
								+ "		}\r\n" + "		@media only screen and (-webkit-device-pixel-ratio:1){\r\n"
								+ "			/* Put CSS for medium density (mdpi) Android layouts in here */\r\n"
								+ "		}\r\n" + "		@media only screen and (-webkit-device-pixel-ratio:1.5){\r\n"
								+ "			/* Put CSS for high density (hdpi) Android layouts in here */\r\n"
								+ "		}\r\n" + "		/* end Android targeting */\r\n" + "		h2{\r\n"
								+ "			color:#181818;\r\n"
								+ "			font-family:Helvetica, Arial, sans-serif;\r\n"
								+ "			font-size:22px;\r\n" + "			line-height: 22px;\r\n"
								+ "			font-weight: normal;\r\n" + "		}\r\n" + "		a.link1{\r\n" + "\r\n"
								+ "		}\r\n" + "		a.link2{\r\n" + "			color:#fff;\r\n"
								+ "			text-decoration:none;\r\n"
								+ "			font-family:Helvetica, Arial, sans-serif;\r\n"
								+ "			font-size:16px;\r\n" + "			color:#fff;border-radius:4px;\r\n"
								+ "		}\r\n" + "		p{\r\n" + "			color:#555;\r\n"
								+ "			font-family:Helvetica, Arial, sans-serif;\r\n"
								+ "			font-size:16px;\r\n" + "			line-height:160%;\r\n" + "		}\r\n"
								+ "	</style>\r\n" + "\r\n" + "<script type=\"colorScheme\" class=\"swatch active\">\r\n"
								+ "  {\r\n" + "    \"name\":\"Default\",\r\n" + "    \"bgBody\":\"ffffff\",\r\n"
								+ "    \"link\":\"fff\",\r\n" + "    \"color\":\"555555\",\r\n"
								+ "    \"bgItem\":\"ffffff\",\r\n" + "    \"title\":\"181818\"\r\n" + "  }\r\n"
								+ "</script>\r\n" + "\r\n" + "</head>\r\n" + "<body>\r\n"
								+ "	<!-- Wrapper/Container Table: Use a wrapper table to control the width and the background color consistently of your email. Use this approach instead of setting attributes on the body tag. -->\r\n"
								+ "	<table cellpadding=\"0\" width=\"100%\" cellspacing=\"0\" border=\"0\" id=\"backgroundTable\" class='bgBody'>\r\n"
								+ "	<tr>\r\n" + "		<td>\r\n"
								+ "	<table cellpadding=\"0\" width=\"620\" class=\"container\" align=\"center\" cellspacing=\"0\" border=\"0\">\r\n"
								+ "	<tr>\r\n" + "		<td>\r\n"
								+ "		<!-- Tables are the most common way to format your email consistently. Set your table widths inside cells and in most cases reset cellpadding, cellspacing, and border to zero. Use nested tables as a way to space effectively in your message. -->\r\n"
								+ "		\r\n" + "\r\n"
								+ "		<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"600\" class=\"container\">\r\n"
								+ "			<tr>\r\n" + "				<td class='movableContentContainer bgItem'>\r\n"
								+ "\r\n" + "					<div class='movableContent'>\r\n"
								+ "						<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"600\" class=\"container\">\r\n"
								+ "							<tr height=\"40\">\r\n"
								+ "								<td width=\"200\">&nbsp;</td>\r\n"
								+ "								<td width=\"200\">&nbsp;</td>\r\n"
								+ "								<td width=\"200\">&nbsp;</td>\r\n"
								+ "							</tr>\r\n" + "							\r\n"
								+ "							<tr height=\"25\">\r\n"
								+ "								<td width=\"200\">&nbsp;</td>\r\n"
								+ "								<td width=\"200\">&nbsp;</td>\r\n"
								+ "								<td width=\"200\">&nbsp;</td>\r\n"
								+ "							</tr>\r\n" + "						</table>\r\n"
								+ "					</div>\r\n" + "\r\n"
								+ "					<div class='movableContent'>\r\n"
								+ "						<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"600\" class=\"container\">\r\n"
								+ "							<tr>\r\n"
								+ "								<td width=\"100%\" colspan=\"3\" align=\"center\" style=\"padding-bottom:10px;padding-top:25px;\">\r\n"
								+ "									<div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "					                	<div class=\"contentEditable\" align='center' >\r\n"
								+ "					                  		<h2 >Hip Hip Hooray!!</h2>\r\n"
								+ "					                	</div>\r\n"
								+ "					              	</div>\r\n"
								+ "								</td>\r\n" + "							</tr>\r\n"
								+ "							<tr>\r\n"
								+ "								<td width=\"100\">&nbsp;</td>\r\n"
								+ "								<td width=\"400\" align=\"center\">\r\n"
								+ "									<div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "					                	<div class=\"contentEditable\" align='left' >\r\n"
								+ "					                  		<p >Hi ,\r\n"
								+ "					                  			<br/>\r\n"
								+ "					                  			<br/>We Got A New Answer To Your Question.'"
								+ thisQuestion.getTopic() + "'.\r\n"
								+ "												Click on the link below to view your searched Question. </p>\r\n"
								+ "					                	</div>\r\n"
								+ "					              	</div>\r\n"
								+ "								</td>\r\n"
								+ "								<td width=\"100\">&nbsp;</td>\r\n"
								+ "							</tr>\r\n" + "						</table>\r\n"
								+ "						<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"600\" class=\"container\">\r\n"
								+ "							<tr>\r\n"
								+ "								<td width=\"200\">&nbsp;</td>\r\n"
								+ "								<td width=\"200\" align=\"center\" style=\"padding-top:25px;\">\r\n"
								+ "									<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"200\" height=\"50\">\r\n"
								+ "										<tr>\r\n"
								+ "											<td bgcolor=\"#1beb11\" align=\"center\" style=\"border-radius:4px;\" width=\"200\" height=\"50\">\r\n"
								+ "												<div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "								                	<div class=\"contentEditable\" align='center' >\r\n"
								+ "								                  		<a target='_blank' href='"
								+ link + "' class='link2'>Go To Site</a>\r\n"
								+ "								                	</div>\r\n"
								+ "								              	</div>\r\n"
								+ "											</td>\r\n"
								+ "										</tr>\r\n"
								+ "									</table>\r\n"
								+ "								</td>\r\n"
								+ "								<td width=\"200\">&nbsp;</td>\r\n"
								+ "							</tr>\r\n" + "						</table>\r\n"
								+ "					</div>\r\n" + "\r\n" + "\r\n"
								+ "					<div class='movableContent'>\r\n"
								+ "						<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"600\" class=\"container\">\r\n"
								+ "							<tr>\r\n"
								+ "								<td width=\"100%\" colspan=\"2\" style=\"padding-top:65px;\">\r\n"
								+ "									<hr style=\"height:1px;border:none;color:#333;background-color:#ddd;\" />\r\n"
								+ "								</td>\r\n" + "							</tr>\r\n"
								+ "							<tr>\r\n"
								+ "								<td width=\"60%\" height=\"70\" valign=\"middle\" style=\"padding-bottom:20px;\">\r\n"
								+ "									<div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "					                	<div class=\"contentEditable\" align='left' >\r\n"
								+ "					                  		<span style=\"font-size:13px;color:#181818;font-family:Helvetica, Arial, sans-serif;line-height:200%;\">by Query Solutions</span>\r\n"
								+ "											<br/>\r\n"
								+ "											\r\n"
								+ "											<span style=\"font-size:13px;color:#181818;font-family:Helvetica, Arial, sans-serif;line-height:200%;\">\r\n"
								+ "											<a target='_blank' href=\"[UNSUBSCRIBE]\" style=\"text-decoration:none;color:#555\">click here to unsubscribe</a></span>\r\n"
								+ "					                	</div>\r\n"
								+ "					              	</div>\r\n"
								+ "								</td>\r\n"
								+ "								<td width=\"40%\" height=\"70\" align=\"right\" valign=\"top\" align='right' style=\"padding-bottom:20px;\">\r\n"
								+ "									<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align='right'>\r\n"
								+ "										\r\n"
								+ "									</table>\r\n"
								+ "								</td>\r\n" + "							</tr>\r\n"
								+ "						</table>\r\n" + "					</div>\r\n" + "\r\n"
								+ "\r\n" + "				</td>\r\n" + "			</tr>\r\n" + "		</table>\r\n"
								+ "\r\n" + "		\r\n" + "		\r\n" + "\r\n" + "	</td></tr></table>\r\n"
								+ "	\r\n" + "		</td>\r\n" + "	</tr>\r\n" + "	</table>\r\n"
								+ "	<!-- End of wrapper table -->\r\n" + "\r\n" + "<!--Default Zone\r\n" + "\r\n"
								+ "    <div class=\"customZone\" data-type=\"image\">\r\n"
								+ "        <div class=\"movableContent\">\r\n"
								+ "        	<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"600\">        		\r\n"
								+ "				<tr><td colspan='3' height='30'></td></tr>\r\n"
								+ "				<tr>\r\n" + "					<td width=\"50\">&nbsp;</td>\r\n"
								+ "					<td width=\"500\" colspan=\"3\" align=\"center\" style=\"padding-bottom:10px;padding-top:25px;\">\r\n"
								+ "						<div class=\"contentEditableContainer contentImageEditable\">\r\n"
								+ "			                <div class=\"contentEditable\">\r\n"
								+ "			                   <img src=\"/applications/Mail_Interface/3_3/modules/User_Interface/core/v31_campaigns/images/neweditor/default/temp_img_1.png\" data-default=\"placeholder\" data-max-width=\"500\">\r\n"
								+ "			                </div>\r\n" + "			            </div>\r\n"
								+ "					</td>\r\n" + "					<td width=\"50\">&nbsp;</td>\r\n"
								+ "				</tr>\r\n" + "			</table>\r\n" + "        </div>\r\n"
								+ "    </div>\r\n" + "\r\n" + "    <div class=\"customZone\" data-type=\"text\">\r\n"
								+ "        <div class='movableContent'>\r\n"
								+ "			<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"600\">\r\n"
								+ "				<tr><td colspan='3' height='30'></td></tr>\r\n"
								+ "				<tr>\r\n" + "					<td width=\"50\">&nbsp;</td>\r\n"
								+ "					<td width=\"500\"  align=\"center\" style=\"padding-bottom:10px;padding-top:25px;\">\r\n"
								+ "						<div class=\"contentEditableContainer contentTextEditable\" >\r\n"
								+ "	                        <div class=\"contentEditable\" >\r\n"
								+ "	                            \r\n"
								+ "								<h2 >Make sure you’re recognizable</h2>\r\n"
								+ "	                        </div>\r\n" + "	                    </div>\r\n"
								+ "					</td>\r\n" + "					<td width=\"50\">&nbsp;</td>\r\n"
								+ "				</tr>\r\n" + "				<tr>\r\n"
								+ "					<td width=\"50\">&nbsp;</td>\r\n"
								+ "					<td width=\"500\" align=\"center\">\r\n"
								+ "						<div class=\"contentEditableContainer contentTextEditable\" >\r\n"
								+ "	                        <div class=\"contentEditable\" >\r\n"
								+ "	                            <p >\r\n"
								+ "								<p>Include both the name of the person who’s sending the email as well as the name of the company, and even better: send using your own domain.</p>\r\n"
								+ "								</p>\r\n" + "	                        </div>\r\n"
								+ "	                    </div>\r\n" + "						\r\n"
								+ "					</td>\r\n" + "					<td width=\"50\">&nbsp;</td>\r\n"
								+ "				</tr>\r\n"
								+ "				<tr><td colspan=\"3\" height='30'></td></tr>\r\n"
								+ "				<tr>\r\n" + "					<td width=\"50\">&nbsp;</td>\r\n"
								+ "					<td width=\"500\" align=\"center\" >\r\n"
								+ "						<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"400\" height=\"50\">\r\n"
								+ "							<tr>\r\n"
								+ "								<td bgcolor=\"#ED006F\" align=\"center\" style=\"border-radius:4px;\" width=\"400\" height=\"50\">\r\n"
								+ "									<div class=\"contentEditableContainer contentTextEditable\" >\r\n"
								+ "				                        <div class=\"contentEditable\" style='text-align:center;'>\r\n"
								+ "				                            <a target='_blank' href=\"[CLIENTS.WEBSITE]\" class='link2'>Read the 3 rules of email marketing sender etiquette</a>\r\n"
								+ "				                        </div>\r\n"
								+ "				                    </div>\r\n"
								+ "								</td>\r\n" + "							</tr>\r\n"
								+ "\r\n" + "						</table>\r\n" + "					</td>\r\n"
								+ "					<td width=\"50\">&nbsp;</td>\r\n" + "				</tr>\r\n"
								+ "				<tr><td height=\"10\" colspan=\"3\"></td></tr>\r\n"
								+ "			</table>\r\n" + "		</div>\r\n" + "    </div>\r\n" + "\r\n"
								+ "    <div class=\"customZone\" data-type=\"imageText\">\r\n"
								+ "        <div class=\"movableContent\">\r\n"
								+ "            <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width='600'>\r\n"
								+ "            	<tr><td colspan=\"5\" height='30'></td></tr>\r\n"
								+ "                <tr>\r\n" + "                	<td width='50'></td>\r\n"
								+ "                    <td valign=\"top\" width='150'>\r\n"
								+ "                        <div class=\"contentEditableContainer contentImageEditable\">\r\n"
								+ "                            <div class=\"contentEditable\">\r\n"
								+ "                                <img src=\"/applications/Mail_Interface/3_3/modules/User_Interface/core/v31_campaigns/images/neweditor/default/temp_img_1.png\" data-default=\"placeholder\" width='150' data-max-width=\"150\">\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='20'></td>\r\n"
								+ "                    <td valign=\"top\"  width='250'>\r\n"
								+ "                        <div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "                            <div class=\"contentEditable\" style=\"color:#555;font-family:Helvetica, Arial, sans-serif;font-size:16px;line-height:160%;\">\r\n"
								+ "                                <h2 >This is a subtitle</h2>\r\n"
								+ "                                <p style=\"text-align:left;\">Etiam bibendum nunc in lacus bibendum porta. Vestibulum nec nulla et eros ornare condimentum. Proin facilisis, dui in mollis blandit. Sed non dui magna, quis tincidunt enim. Morbi vehicula pharetra lacinia.</p>\r\n"
								+ "                                <p style=\"text-align:right;\"><a target='_blank' href=\"\">Read more</a></p>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='50'></td>\r\n"
								+ "                </tr>\r\n" + "            </table>\r\n" + "        </div>\r\n"
								+ "    </div>\r\n" + "\r\n"
								+ "    <div class=\"customZone\" data-type=\"Textimage\">\r\n"
								+ "        <div class=\"movableContent\">\r\n"
								+ "            <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width='600'>\r\n"
								+ "            	<tr><td colspan=\"5\" height='30'></td></tr>\r\n"
								+ "                <tr>\r\n" + "                	<td width='50'></td>\r\n"
								+ "                    <td valign=\"top\" width='230'>\r\n"
								+ "                        <div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "                            <div class=\"contentEditable\" style=\"color:#555;font-family:Helvetica, Arial, sans-serif;font-size:16px;line-height:160%;\">\r\n"
								+ "                                <h2 >This is a subtitle</h2>\r\n"
								+ "                                <p style=\"text-align:left;\">Etiam bibendum nunc in lacus bibendum porta. Vestibulum nec nulla et eros ornare condimentum. Proin facilisis, dui in mollis blandit. Sed non dui magna, quis tincidunt enim. Morbi vehicula pharetra lacinia. </p>\r\n"
								+ "                                <p style=\"text-align:right;\"><a target='_blank' href=\"\">Read more</a></p>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='20'></td>\r\n"
								+ "                    <td valign=\"top\" width='150'>\r\n"
								+ "                        <div class=\"contentEditableContainer contentImageEditable\">\r\n"
								+ "                            <div class=\"contentEditable\">\r\n"
								+ "                                <img src=\"/applications/Mail_Interface/3_3/modules/User_Interface/core/v31_campaigns/images/neweditor/default/temp_img_1.png\" data-default=\"placeholder\" width='150' data-max-width=\"150\">\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='50'></td>\r\n"
								+ "                </tr>\r\n" + "            </table>\r\n" + "        </div>\r\n"
								+ "    </div>\r\n" + "\r\n"
								+ "    <div class=\"customZone\" data-type=\"textText\">\r\n"
								+ "        <div class=\"movableContent\">\r\n"
								+ "            <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width='600'>\r\n"
								+ "            	<tr><td height='30' colspan='5'></td></tr>\r\n"
								+ "                <tr>\r\n" + "                	<td width='50'></td>\r\n"
								+ "                    <td valign=\"top\" width=\"230\">\r\n"
								+ "                        <div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "                            <div class=\"contentEditable\" style=\"color:#555;font-family:Helvetica, Arial, sans-serif;font-size:16px;line-height:160%;\">\r\n"
								+ "                                <h2 >This is a subtitle</h2>\r\n"
								+ "                                <p >Etiam bibendum nunc in lacus bibendum porta. Vestibulum nec nulla et eros ornare condimentum. Proin facilisis, dui in mollis blandit. Sed non dui magna, quis tincidunt enim.</p>\r\n"
								+ "                                <p style=\"text-align:right;\"><a target='_blank' href=\"\">Read more</a></p>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='40'></td>\r\n"
								+ "                    <td valign=\"top\" width='230'>\r\n"
								+ "                        <div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "                            <div class=\"contentEditable\" style=\"color:#555;font-family:Helvetica, Arial, sans-serif;font-size:16px;line-height:160%;\">\r\n"
								+ "                                <h2 >This is a subtitle</h2>\r\n"
								+ "                                <p style=\"text-align:left;\">Etiam bibendum nunc in lacus bibendum porta. Vestibulum nec nulla et eros ornare condimentum. Proin facilisis, dui in mollis blandit. Sed non dui magna, quis tincidunt enim.</p>\r\n"
								+ "                                <p style=\"text-align:right;\"><a target='_blank' href=\"\">Read more</a></p>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='50'></td>\r\n"
								+ "                </tr>\r\n" + "            </table>\r\n" + "        </div>\r\n"
								+ "    </div>\r\n" + "\r\n" + "    <div class=\"customZone\" data-type=\"qrcode\">\r\n"
								+ "        <div class=\"movableContent\">\r\n"
								+ "            <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width='600'>\r\n"
								+ "            	<tr><td height='30' colspan='5'></td></tr>\r\n"
								+ "                <tr>\r\n" + "                	<td width='50'></td>\r\n"
								+ "                    <td valign=\"top\" >\r\n"
								+ "                        <div class=\"contentQrcodeEditable contentEditableContainer\">\r\n"
								+ "                            <div class=\"contentEditable\">\r\n"
								+ "                                <img src=\"/applications/Mail_Interface/3_3/modules/User_Interface/core/v31_campaigns/images/neweditor/default/qr_code.png\" width=\"75\" height=\"75\" data-default=\"placeholder\">\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='20'></td>\r\n"
								+ "                    <td valign=\"top\">\r\n"
								+ "                        <div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "                            <div class=\"contentEditable\" style=\"color:#555;font-family:Helvetica, Arial, sans-serif;font-size:16px;line-height:160%;\">\r\n"
								+ "                                <h2 >This is a subtitle</h2>\r\n"
								+ "                                <p style=\"text-align:left;\">Etiam bibendum nunc in lacus bibendum porta. Vestibulum nec nulla et eros ornare condimentum. Proin facilisis, dui in mollis blandit. Sed non dui magna, quis tincidunt enim. Morbi vehicula pharetra lacinia. Cras tincidunt, justo at fermentum feugiat, eros orci accumsan dolor, eu ultricies eros dolor quis sapien. Curabitur in turpis sem, a sodales purus. Pellentesque et risus at mauris aliquet gravida.</p>\r\n"
								+ "                                <p style=\"text-align:left;\">Integer in elit in tortor posuere molestie non a velit. Pellentesque consectetur, nisi a euismod scelerisque.</p>\r\n"
								+ "                                <p style=\"text-align:right;\"><a target='_blank' href=\"\">Read more</a></p>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='50'></td>\r\n"
								+ "                </tr>\r\n" + "            </table>\r\n" + "        </div>\r\n"
								+ "    </div>\r\n" + "\r\n" + "    <div class=\"customZone\" data-type=\"social\">\r\n"
								+ "        <div class=\"movableContent\" align='center'>\r\n"
								+ "            <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width='600'>\r\n"
								+ "            	<tr><td height='30' colspan='5'></td></tr>\r\n"
								+ "                <tr>\r\n" + "                	<td width='50'></td>\r\n"
								+ "                    <td valign=\"top\" width=\"230\" align='center'>\r\n"
								+ "                        <div class=\"contentEditableContainer contentFacebookEditable\">\r\n"
								+ "                            <div class=\"contentEditable\">\r\n"
								+ "                                <img data-default=\"placeholder\" src=\"images/facebook.png\" data-max-width='60' data-customIcon=\"true\" data-noText=\"true\" width='60' height='60'>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                        <div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "                            <div class=\"contentEditable\" style=\"color:#555;font-family:Helvetica, Arial, sans-serif;font-size:16px;line-height:160%;\">\r\n"
								+ "                                <h2 >Facebook</h2>\r\n"
								+ "                                <p>Like us on Facebook to keep up with our news, updates and other discussions.</p>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='40'></td>\r\n"
								+ "                    <td valign=\"top\" width=\"230\" align='center'>\r\n"
								+ "                        <div class=\"contentEditableContainer contentTwitterEditable\">\r\n"
								+ "                            <div class=\"contentEditable\">\r\n"
								+ "                                <img data-default=\"placeholder\" src=\"images/twitter.png\" data-max-width='60' data-customIcon=\"true\" data-noText=\"true\" width='60' height='60'>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                        <div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "                            <div class=\"contentEditable\" style=\"color:#555;font-family:Helvetica, Arial, sans-serif;font-size:16px;line-height:160%;\">\r\n"
								+ "                                <h2 >Twitter</h2>\r\n"
								+ "                                <p>Follow us on twitter to stay up to date with company news and other information.</p>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='50'></td>\r\n"
								+ "                </tr>\r\n" + "            </table>\r\n" + "        </div>\r\n"
								+ "    </div>\r\n" + "\r\n" + "    <div class=\"customZone\" data-type=\"twitter\">\r\n"
								+ "        <div class=\"movableContent\" align='center'>\r\n"
								+ "            <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width='600'>\r\n"
								+ "            	<tr><td height='30' colspan='3'></td></tr>\r\n"
								+ "                <tr>\r\n" + "                	<td width='50'></td>\r\n"
								+ "                    <td valign=\"top\" align='center'>\r\n"
								+ "                        <div class=\"contentEditableContainer contentTwitterEditable\">\r\n"
								+ "                            <div class=\"contentEditable\">\r\n"
								+ "                                <img data-default=\"placeholder\" src=\"images/twitter.png\" data-max-width='60' data-customIcon=\"true\" data-noText=\"true\" width='60' height='60'>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                        <div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "                            <div class=\"contentEditable\" style=\"color:#555;font-family:Helvetica, Arial, sans-serif;font-size:16px;line-height:160%;\">\r\n"
								+ "                                <h2 >Twitter</h2>\r\n"
								+ "                                <p>Follow us on twitter to stay up to date with company news and other information.</p>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='50'></td>\r\n"
								+ "                </tr>\r\n" + "            </table>\r\n" + "        </div>\r\n"
								+ "   </div>\r\n" + "\r\n"
								+ "    <div class=\"customZone\" data-type=\"facebook\" >\r\n"
								+ "        <div class=\"movableContent\" align='center'>\r\n"
								+ "            <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width='600'>\r\n"
								+ "            	<tr><td height='30' colspan='3'></td></tr>\r\n"
								+ "                <tr>\r\n" + "                	<td width='50'></td>\r\n"
								+ "                    <td valign=\"top\" align='center'>\r\n"
								+ "                        <div class=\"contentEditableContainer contentFacebookEditable\">\r\n"
								+ "                            <div class=\"contentEditable\">\r\n"
								+ "                                <img data-default=\"placeholder\" src=\"images/facebook.png\" data-max-width='60' data-customIcon=\"true\" data-noText=\"true\" width='60' height='60'>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                        <div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "                            <div class=\"contentEditable\" style=\"color:#555;font-family:Helvetica, Arial, sans-serif;font-size:16px;line-height:160%;\">\r\n"
								+ "                                <h2>Facebook</h2>\r\n"
								+ "                                <p>Like us on Facebook to keep up with our news, updates and other discussions.</p>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='50'></td>\r\n"
								+ "                </tr>\r\n" + "            </table>\r\n" + "        </div>\r\n"
								+ "    </div>\r\n" + "\r\n" + "    <div class=\"customZone\" data-type=\"gmap\">\r\n"
								+ "        <div class=\"movableContent\">\r\n"
								+ "            <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width='600'>\r\n"
								+ "                <tr><td height='30' colspan='5'></td></tr>\r\n"
								+ "                <tr>\r\n" + "                	<td width='50'></td>\r\n"
								+ "                    <td valign=\"top\" >\r\n"
								+ "                        <div class=\"contentGmapEditable contentEditableContainer\">\r\n"
								+ "                            <div class=\"contentEditable\">\r\n"
								+ "                                <img src=\"/applications/Mail_Interface/3_3/modules/User_Interface/core/v31_campaigns/images/neweditor/default/gmap_example.png\" width=\"75\" data-default=\"placeholder\">\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='20'></td>\r\n"
								+ "                    <td valign=\"top\">\r\n"
								+ "                        <div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "                            <div class=\"contentEditable\" style=\"color:#555;font-family:Helvetica, Arial, sans-serif;font-size:16px;line-height:160%;\">\r\n"
								+ "                                <h2 >This is a subtitle</h2>\r\n"
								+ "                                <p style=\"text-align:left;\">Etiam bibendum nunc in lacus bibendum porta. Vestibulum nec nulla et eros ornare condimentum. Proin facilisis, dui in mollis blandit. Sed non dui magna, quis tincidunt enim. Morbi vehicula pharetra lacinia. Cras tincidunt, justo at fermentum feugiat, eros orci accumsan dolor, eu ultricies eros dolor quis sapien. Curabitur in turpis sem, a sodales purus. Pellentesque et risus at mauris aliquet gravida.</p>\r\n"
								+ "                                <p style=\"text-align:left;\">Integer in elit in tortor posuere molestie non a velit. Pellentesque consectetur, nisi a euismod scelerisque.</p>\r\n"
								+ "                                <p style=\"text-align:right;\"><a target='_blank' href=\"\">Read more</a></p>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='50'></td>\r\n"
								+ "                </tr>\r\n" + "            </table>\r\n" + "        </div>\r\n"
								+ "    </div>\r\n" + "\r\n" + "\r\n"
								+ "	 <div class=\"customZone\" data-type=\"colums1v2\"><div class='movableContent'>\r\n"
								+ "          	<table width=\"600\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\" >\r\n"
								+ "	            <tr><td height=\"30\" colspan='3'>&nbsp;</td></tr>\r\n"
								+ "	            <tr>\r\n" + "	            	<td width='50'></td>\r\n"
								+ "	              	<td width='500' align=\"center\" valign=\"top\" class=\"newcontent\">\r\n"
								+ "	                \r\n" + "	              	</td>\r\n"
								+ "	              	<td width='50'></td>\r\n" + "	            </tr>\r\n"
								+ "          	</table>\r\n" + "    	</div>\r\n" + "      </div>\r\n" + "\r\n"
								+ "      <div class=\"customZone\" data-type=\"colums2v2\"><div class='movableContent'>\r\n"
								+ "          <table width=\"600\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\" >\r\n"
								+ "	            <tr><td height=\"30\" colspan='3'>&nbsp;</td></tr>\r\n"
								+ "	            <tr>\r\n" + "	            	<td width='50'></td>\r\n"
								+ "	              	<td width='235' align=\"center\" valign=\"top\" class=\"newcontent\">\r\n"
								+ "	                \r\n" + "	              	</td>\r\n"
								+ "					<td width='30'></td>\r\n"
								+ "	              	<td width='235' align=\"center\" valign=\"top\" class=\"newcontent\">\r\n"
								+ "	                \r\n" + "	              	</td>\r\n"
								+ "	              	<td width='50'></td>\r\n" + "	            </tr>\r\n"
								+ "          	</table>\r\n" + "    	</div>\r\n" + "      </div>\r\n" + "\r\n"
								+ "      <div class=\"customZone\" data-type=\"colums3v2\"><div class='movableContent'>\r\n"
								+ "         <table width=\"600\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\" >\r\n"
								+ "	            <tr><td height=\"30\" colspan='3'>&nbsp;</td></tr>\r\n"
								+ "	            <tr>\r\n" + "	            	<td width='50'></td>\r\n"
								+ "	              	<td width='158' align=\"center\" valign=\"top\" class=\"newcontent\">\r\n"
								+ "	                \r\n" + "	              	</td>\r\n"
								+ "					<td width='12'></td>\r\n"
								+ "	              	<td width='158' align=\"center\" valign=\"top\" class=\"newcontent\">\r\n"
								+ "	                \r\n" + "	              	</td>\r\n"
								+ "					<td width='12'></td>\r\n"
								+ "	              	<td width='158' align=\"center\" valign=\"top\" class=\"newcontent\">\r\n"
								+ "	                \r\n" + "	              	</td>\r\n"
								+ "	              	<td width='50'></td>\r\n" + "	            </tr>\r\n"
								+ "          	</table>\r\n" + "    	</div>\r\n" + "      </div>\r\n" + "\r\n"
								+ "      <div class=\"customZone\" data-type=\"textv2\">\r\n"
								+ "		<div class=\"contentEditableContainer contentTextEditable\" >\r\n"
								+ "            <div class=\"contentEditable\" >\r\n" + "                \r\n"
								+ "				<h2 >Make sure you’re recognizable</h2>\r\n" + "            </div>\r\n"
								+ "        </div>\r\n"
								+ "		<div class=\"contentEditableContainer contentTextEditable\" >\r\n"
								+ "            <div class=\"contentEditable\" >\r\n"
								+ "				<p>Include both the name of the person who’s sending the email as well as the name of the company, and even better: send using your own domain.</p>\r\n"
								+ "            </div>\r\n" + "        </div>\r\n"
								+ "		<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"79%\" height=\"50\">\r\n"
								+ "			<tr>\r\n"
								+ "				<td bgcolor=\"#ED006F\" align=\"center\" style=\"border-radius:4px;\" width=\"100%\" height=\"50\">\r\n"
								+ "					<div class=\"contentEditableContainer contentTextEditable\" >\r\n"
								+ "                        <div class=\"contentEditable\" style='text-align:center;'>\r\n"
								+ "                            <a target='_blank' href=\"[CLIENTS.WEBSITE]\" class='link2'>Read the 3 rules of email marketing sender etiquette</a>\r\n"
								+ "                        </div>\r\n" + "                    </div>\r\n"
								+ "				</td>\r\n" + "			</tr>\r\n" + "		</table>\r\n"
								+ "      </div>\r\n" + "\r\n" + "\r\n" + "\r\n" + "\r\n" + "-->\r\n"
								+ "<!--Default Zone End-->\r\n" + "\r\n" + "</body>\r\n" + "</html>",
						true);
				sender.send(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void mailToValidate(Publisher publisher, HttpServletRequest request) {
		MimeMessage msg = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(msg);

		try {
//			send mail.. to the respective publisher
			String link = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
					+ request.getContextPath() + "/validateAccount?uniqueId=" + publisher.getUniqueId() +"&publisherId="+ publisher.getId();
			
				helper.setTo(publisher.getUsername());
				helper.setSubject("Hi From QuerySolutions.");
				helper.setText(
						"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\r\n"
								+ "\r\n" + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\r\n" + "<head>\r\n"
								+ "	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\r\n"
								+ "	<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\r\n"
								+ "	<title>[SUBJECT]</title>\r\n" + "	<style type=\"text/css\">\r\n" + "\r\n"
								+ "@media screen and (max-width: 600px) {\r\n" + "    table[class=\"container\"] {\r\n"
								+ "        width: 95% !important;\r\n" + "    }\r\n" + "}\r\n" + "\r\n"
								+ "	#outlook a {padding:0;}\r\n"
								+ "		body{width:100% !important; -webkit-text-size-adjust:100%; -ms-text-size-adjust:100%; margin:0; padding:0;}\r\n"
								+ "		.ExternalClass {width:100%;}\r\n"
								+ "		.ExternalClass, .ExternalClass p, .ExternalClass span, .ExternalClass font, .ExternalClass td, .ExternalClass div {line-height: 100%;}\r\n"
								+ "		#backgroundTable {margin:0; padding:0; width:100% !important; line-height: 100% !important;}\r\n"
								+ "		img {outline:none; text-decoration:none; -ms-interpolation-mode: bicubic;}\r\n"
								+ "		a img {border:none;}\r\n" + "		.image_fix {display:block;}\r\n"
								+ "		p {margin: 1em 0;}\r\n"
								+ "		h1, h2, h3, h4, h5, h6 {color: black !important;}\r\n" + "\r\n"
								+ "		h1 a, h2 a, h3 a, h4 a, h5 a, h6 a {color: blue !important;}\r\n" + "\r\n"
								+ "		h1 a:active, h2 a:active,  h3 a:active, h4 a:active, h5 a:active, h6 a:active {\r\n"
								+ "			color: red !important; \r\n" + "		 }\r\n" + "\r\n"
								+ "		h1 a:visited, h2 a:visited,  h3 a:visited, h4 a:visited, h5 a:visited, h6 a:visited {\r\n"
								+ "			color: purple !important; \r\n" + "		}\r\n" + "\r\n"
								+ "		table td {border-collapse: collapse;}\r\n" + "\r\n"
								+ "		table { border-collapse:collapse; mso-table-lspace:0pt; mso-table-rspace:0pt; }\r\n"
								+ "\r\n" + "		a {color: #000;}\r\n" + "\r\n"
								+ "		@media only screen and (max-device-width: 480px) {\r\n" + "\r\n"
								+ "			a[href^=\"tel\"], a[href^=\"sms\"] {\r\n"
								+ "						text-decoration: none;\r\n"
								+ "						color: black; /* or whatever your want */\r\n"
								+ "						pointer-events: none;\r\n"
								+ "						cursor: default;\r\n" + "					}\r\n" + "\r\n"
								+ "			.mobile_link a[href^=\"tel\"], .mobile_link a[href^=\"sms\"] {\r\n"
								+ "						text-decoration: default;\r\n"
								+ "						color: orange !important; /* or whatever your want */\r\n"
								+ "						pointer-events: auto;\r\n"
								+ "						cursor: default;\r\n" + "					}\r\n"
								+ "		}\r\n" + "\r\n" + "\r\n"
								+ "		@media only screen and (min-device-width: 768px) and (max-device-width: 1024px) {\r\n"
								+ "			a[href^=\"tel\"], a[href^=\"sms\"] {\r\n"
								+ "						text-decoration: none;\r\n"
								+ "						color: blue; /* or whatever your want */\r\n"
								+ "						pointer-events: none;\r\n"
								+ "						cursor: default;\r\n" + "					}\r\n" + "\r\n"
								+ "			.mobile_link a[href^=\"tel\"], .mobile_link a[href^=\"sms\"] {\r\n"
								+ "						text-decoration: default;\r\n"
								+ "						color: orange !important;\r\n"
								+ "						pointer-events: auto;\r\n"
								+ "						cursor: default;\r\n" + "					}\r\n"
								+ "		}\r\n" + "\r\n"
								+ "		@media only screen and (-webkit-min-device-pixel-ratio: 2) {\r\n"
								+ "			/* Put your iPhone 4g styles in here */\r\n" + "		}\r\n" + "\r\n"
								+ "		@media only screen and (-webkit-device-pixel-ratio:.75){\r\n"
								+ "			/* Put CSS for low density (ldpi) Android layouts in here */\r\n"
								+ "		}\r\n" + "		@media only screen and (-webkit-device-pixel-ratio:1){\r\n"
								+ "			/* Put CSS for medium density (mdpi) Android layouts in here */\r\n"
								+ "		}\r\n" + "		@media only screen and (-webkit-device-pixel-ratio:1.5){\r\n"
								+ "			/* Put CSS for high density (hdpi) Android layouts in here */\r\n"
								+ "		}\r\n" + "		/* end Android targeting */\r\n" + "		h2{\r\n"
								+ "			color:#181818;\r\n"
								+ "			font-family:Helvetica, Arial, sans-serif;\r\n"
								+ "			font-size:22px;\r\n" + "			line-height: 22px;\r\n"
								+ "			font-weight: normal;\r\n" + "		}\r\n" + "		a.link1{\r\n" + "\r\n"
								+ "		}\r\n" + "		a.link2{\r\n" + "			color:#fff;\r\n"
								+ "			text-decoration:none;\r\n"
								+ "			font-family:Helvetica, Arial, sans-serif;\r\n"
								+ "			font-size:16px;\r\n" + "			color:#fff;border-radius:4px;\r\n"
								+ "		}\r\n" + "		p{\r\n" + "			color:#555;\r\n"
								+ "			font-family:Helvetica, Arial, sans-serif;\r\n"
								+ "			font-size:16px;\r\n" + "			line-height:160%;\r\n" + "		}\r\n"
								+ "	</style>\r\n" + "\r\n" + "<script type=\"colorScheme\" class=\"swatch active\">\r\n"
								+ "  {\r\n" + "    \"name\":\"Default\",\r\n" + "    \"bgBody\":\"ffffff\",\r\n"
								+ "    \"link\":\"fff\",\r\n" + "    \"color\":\"555555\",\r\n"
								+ "    \"bgItem\":\"ffffff\",\r\n" + "    \"title\":\"181818\"\r\n" + "  }\r\n"
								+ "</script>\r\n" + "\r\n" + "</head>\r\n" + "<body>\r\n"
								+ "	<!-- Wrapper/Container Table: Use a wrapper table to control the width and the background color consistently of your email. Use this approach instead of setting attributes on the body tag. -->\r\n"
								+ "	<table cellpadding=\"0\" width=\"100%\" cellspacing=\"0\" border=\"0\" id=\"backgroundTable\" class='bgBody'>\r\n"
								+ "	<tr>\r\n" + "		<td>\r\n"
								+ "	<table cellpadding=\"0\" width=\"620\" class=\"container\" align=\"center\" cellspacing=\"0\" border=\"0\">\r\n"
								+ "	<tr>\r\n" + "		<td>\r\n"
								+ "		<!-- Tables are the most common way to format your email consistently. Set your table widths inside cells and in most cases reset cellpadding, cellspacing, and border to zero. Use nested tables as a way to space effectively in your message. -->\r\n"
								+ "		\r\n" + "\r\n"
								+ "		<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"600\" class=\"container\">\r\n"
								+ "			<tr>\r\n" + "				<td class='movableContentContainer bgItem'>\r\n"
								+ "\r\n" + "					<div class='movableContent'>\r\n"
								+ "						<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"600\" class=\"container\">\r\n"
								+ "							<tr height=\"40\">\r\n"
								+ "								<td width=\"200\">&nbsp;</td>\r\n"
								+ "								<td width=\"200\">&nbsp;</td>\r\n"
								+ "								<td width=\"200\">&nbsp;</td>\r\n"
								+ "							</tr>\r\n" + "							\r\n"
								+ "							<tr height=\"25\">\r\n"
								+ "								<td width=\"200\">&nbsp;</td>\r\n"
								+ "								<td width=\"200\">&nbsp;</td>\r\n"
								+ "								<td width=\"200\">&nbsp;</td>\r\n"
								+ "							</tr>\r\n" + "						</table>\r\n"
								+ "					</div>\r\n" + "\r\n"
								+ "					<div class='movableContent'>\r\n"
								+ "						<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"600\" class=\"container\">\r\n"
								+ "							<tr>\r\n"
								+ "								<td width=\"100%\" colspan=\"3\" align=\"center\" style=\"padding-bottom:10px;padding-top:25px;\">\r\n"
								+ "									<div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "					                	<div class=\"contentEditable\" align='center' >\r\n"
								+ "					                  		<h2 >Hi "+publisher.getName()+" !!</h2>\r\n"
								+ "					                	</div>\r\n"
								+ "					              	</div>\r\n"
								+ "								</td>\r\n" + "							</tr>\r\n"
								+ "							<tr>\r\n"
								+ "								<td width=\"100\">&nbsp;</td>\r\n"
								+ "								<td width=\"400\" align=\"center\">\r\n"
								+ "									<div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "					                	<div class=\"contentEditable\" align='left' >\r\n"
								+ "					                  		<p>"
								+ "					                  			<br/>\r\n"
								+ "					                  			<br/>Click the link below to join with us. </p>\r\n"
								+ "					                	</div>\r\n"
								+ "					              	</div>\r\n"
								+ "								</td>\r\n"
								+ "								<td width=\"100\">&nbsp;</td>\r\n"
								+ "							</tr>\r\n" + "						</table>\r\n"
								+ "						<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"600\" class=\"container\">\r\n"
								+ "							<tr>\r\n"
								+ "								<td width=\"200\">&nbsp;</td>\r\n"
								+ "								<td width=\"200\" align=\"center\" style=\"padding-top:25px;\">\r\n"
								+ "									<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"200\" height=\"50\">\r\n"
								+ "										<tr>\r\n"
								+ "											<td bgcolor=\"#1beb11\" align=\"center\" style=\"border-radius:4px;\" width=\"200\" height=\"50\">\r\n"
								+ "												<div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "								                	<div class=\"contentEditable\" align='center' >\r\n"
								+ "								                  		<a target='_blank' href='"
								+ link + "' class='link2'>Acivate My Account</a>\r\n"
								+ "								                	</div>\r\n"
								+ "								              	</div>\r\n"
								+ "											</td>\r\n"
								+ "										</tr>\r\n"
								+ "									</table>\r\n"
								+ "								</td>\r\n"
								+ "								<td width=\"200\">&nbsp;</td>\r\n"
								+ "							</tr>\r\n" + "						</table>\r\n"
								+ "					</div>\r\n" + "\r\n" + "\r\n"
								+ "					<div class='movableContent'>\r\n"
								+ "						<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"600\" class=\"container\">\r\n"
								+ "							<tr>\r\n"
								+ "								<td width=\"100%\" colspan=\"2\" style=\"padding-top:65px;\">\r\n"
								+ "									<hr style=\"height:1px;border:none;color:#333;background-color:#ddd;\" />\r\n"
								+ "								</td>\r\n" + "							</tr>\r\n"
								+ "							<tr>\r\n"
								+ "								<td width=\"60%\" height=\"70\" valign=\"middle\" style=\"padding-bottom:20px;\">\r\n"
								+ "									<div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "					                	<div class=\"contentEditable\" align='left' >\r\n"
								+ "					                  		<span style=\"font-size:13px;color:#181818;font-family:Helvetica, Arial, sans-serif;line-height:200%;\">by Query Solutions</span>\r\n"
								+ "											<br/>\r\n"
								+ "											\r\n"
								+ "											<span style=\"font-size:13px;color:#181818;font-family:Helvetica, Arial, sans-serif;line-height:200%;\">\r\n"
								+ "											<a target='_blank' href=\"[UNSUBSCRIBE]\" style=\"text-decoration:none;color:#555\">click here to unsubscribe</a></span>\r\n"
								+ "					                	</div>\r\n"
								+ "					              	</div>\r\n"
								+ "								</td>\r\n"
								+ "								<td width=\"40%\" height=\"70\" align=\"right\" valign=\"top\" align='right' style=\"padding-bottom:20px;\">\r\n"
								+ "									<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align='right'>\r\n"
								+ "										\r\n"
								+ "									</table>\r\n"
								+ "								</td>\r\n" + "							</tr>\r\n"
								+ "						</table>\r\n" + "					</div>\r\n" + "\r\n"
								+ "\r\n" + "				</td>\r\n" + "			</tr>\r\n" + "		</table>\r\n"
								+ "\r\n" + "		\r\n" + "		\r\n" + "\r\n" + "	</td></tr></table>\r\n"
								+ "	\r\n" + "		</td>\r\n" + "	</tr>\r\n" + "	</table>\r\n"
								+ "	<!-- End of wrapper table -->\r\n" + "\r\n" + "<!--Default Zone\r\n" + "\r\n"
								+ "    <div class=\"customZone\" data-type=\"image\">\r\n"
								+ "        <div class=\"movableContent\">\r\n"
								+ "        	<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"600\">        		\r\n"
								+ "				<tr><td colspan='3' height='30'></td></tr>\r\n"
								+ "				<tr>\r\n" + "					<td width=\"50\">&nbsp;</td>\r\n"
								+ "					<td width=\"500\" colspan=\"3\" align=\"center\" style=\"padding-bottom:10px;padding-top:25px;\">\r\n"
								+ "						<div class=\"contentEditableContainer contentImageEditable\">\r\n"
								+ "			                <div class=\"contentEditable\">\r\n"
								+ "			                   <img src=\"/applications/Mail_Interface/3_3/modules/User_Interface/core/v31_campaigns/images/neweditor/default/temp_img_1.png\" data-default=\"placeholder\" data-max-width=\"500\">\r\n"
								+ "			                </div>\r\n" + "			            </div>\r\n"
								+ "					</td>\r\n" + "					<td width=\"50\">&nbsp;</td>\r\n"
								+ "				</tr>\r\n" + "			</table>\r\n" + "        </div>\r\n"
								+ "    </div>\r\n" + "\r\n" + "    <div class=\"customZone\" data-type=\"text\">\r\n"
								+ "        <div class='movableContent'>\r\n"
								+ "			<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"600\">\r\n"
								+ "				<tr><td colspan='3' height='30'></td></tr>\r\n"
								+ "				<tr>\r\n" + "					<td width=\"50\">&nbsp;</td>\r\n"
								+ "					<td width=\"500\"  align=\"center\" style=\"padding-bottom:10px;padding-top:25px;\">\r\n"
								+ "						<div class=\"contentEditableContainer contentTextEditable\" >\r\n"
								+ "	                        <div class=\"contentEditable\" >\r\n"
								+ "	                            \r\n"
								+ "								<h2 >Make sure you’re recognizable</h2>\r\n"
								+ "	                        </div>\r\n" + "	                    </div>\r\n"
								+ "					</td>\r\n" + "					<td width=\"50\">&nbsp;</td>\r\n"
								+ "				</tr>\r\n" + "				<tr>\r\n"
								+ "					<td width=\"50\">&nbsp;</td>\r\n"
								+ "					<td width=\"500\" align=\"center\">\r\n"
								+ "						<div class=\"contentEditableContainer contentTextEditable\" >\r\n"
								+ "	                        <div class=\"contentEditable\" >\r\n"
								+ "	                            <p >\r\n"
								+ "								<p>Include both the name of the person who’s sending the email as well as the name of the company, and even better: send using your own domain.</p>\r\n"
								+ "								</p>\r\n" + "	                        </div>\r\n"
								+ "	                    </div>\r\n" + "						\r\n"
								+ "					</td>\r\n" + "					<td width=\"50\">&nbsp;</td>\r\n"
								+ "				</tr>\r\n"
								+ "				<tr><td colspan=\"3\" height='30'></td></tr>\r\n"
								+ "				<tr>\r\n" + "					<td width=\"50\">&nbsp;</td>\r\n"
								+ "					<td width=\"500\" align=\"center\" >\r\n"
								+ "						<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"400\" height=\"50\">\r\n"
								+ "							<tr>\r\n"
								+ "								<td bgcolor=\"#ED006F\" align=\"center\" style=\"border-radius:4px;\" width=\"400\" height=\"50\">\r\n"
								+ "									<div class=\"contentEditableContainer contentTextEditable\" >\r\n"
								+ "				                        <div class=\"contentEditable\" style='text-align:center;'>\r\n"
								+ "				                            <a target='_blank' href=\"[CLIENTS.WEBSITE]\" class='link2'>Read the 3 rules of email marketing sender etiquette</a>\r\n"
								+ "				                        </div>\r\n"
								+ "				                    </div>\r\n"
								+ "								</td>\r\n" + "							</tr>\r\n"
								+ "\r\n" + "						</table>\r\n" + "					</td>\r\n"
								+ "					<td width=\"50\">&nbsp;</td>\r\n" + "				</tr>\r\n"
								+ "				<tr><td height=\"10\" colspan=\"3\"></td></tr>\r\n"
								+ "			</table>\r\n" + "		</div>\r\n" + "    </div>\r\n" + "\r\n"
								+ "    <div class=\"customZone\" data-type=\"imageText\">\r\n"
								+ "        <div class=\"movableContent\">\r\n"
								+ "            <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width='600'>\r\n"
								+ "            	<tr><td colspan=\"5\" height='30'></td></tr>\r\n"
								+ "                <tr>\r\n" + "                	<td width='50'></td>\r\n"
								+ "                    <td valign=\"top\" width='150'>\r\n"
								+ "                        <div class=\"contentEditableContainer contentImageEditable\">\r\n"
								+ "                            <div class=\"contentEditable\">\r\n"
								+ "                                <img src=\"/applications/Mail_Interface/3_3/modules/User_Interface/core/v31_campaigns/images/neweditor/default/temp_img_1.png\" data-default=\"placeholder\" width='150' data-max-width=\"150\">\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='20'></td>\r\n"
								+ "                    <td valign=\"top\"  width='250'>\r\n"
								+ "                        <div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "                            <div class=\"contentEditable\" style=\"color:#555;font-family:Helvetica, Arial, sans-serif;font-size:16px;line-height:160%;\">\r\n"
								+ "                                <h2 >This is a subtitle</h2>\r\n"
								+ "                                <p style=\"text-align:left;\">Etiam bibendum nunc in lacus bibendum porta. Vestibulum nec nulla et eros ornare condimentum. Proin facilisis, dui in mollis blandit. Sed non dui magna, quis tincidunt enim. Morbi vehicula pharetra lacinia.</p>\r\n"
								+ "                                <p style=\"text-align:right;\"><a target='_blank' href=\"\">Read more</a></p>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='50'></td>\r\n"
								+ "                </tr>\r\n" + "            </table>\r\n" + "        </div>\r\n"
								+ "    </div>\r\n" + "\r\n"
								+ "    <div class=\"customZone\" data-type=\"Textimage\">\r\n"
								+ "        <div class=\"movableContent\">\r\n"
								+ "            <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width='600'>\r\n"
								+ "            	<tr><td colspan=\"5\" height='30'></td></tr>\r\n"
								+ "                <tr>\r\n" + "                	<td width='50'></td>\r\n"
								+ "                    <td valign=\"top\" width='230'>\r\n"
								+ "                        <div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "                            <div class=\"contentEditable\" style=\"color:#555;font-family:Helvetica, Arial, sans-serif;font-size:16px;line-height:160%;\">\r\n"
								+ "                                <h2 >This is a subtitle</h2>\r\n"
								+ "                                <p style=\"text-align:left;\">Etiam bibendum nunc in lacus bibendum porta. Vestibulum nec nulla et eros ornare condimentum. Proin facilisis, dui in mollis blandit. Sed non dui magna, quis tincidunt enim. Morbi vehicula pharetra lacinia. </p>\r\n"
								+ "                                <p style=\"text-align:right;\"><a target='_blank' href=\"\">Read more</a></p>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='20'></td>\r\n"
								+ "                    <td valign=\"top\" width='150'>\r\n"
								+ "                        <div class=\"contentEditableContainer contentImageEditable\">\r\n"
								+ "                            <div class=\"contentEditable\">\r\n"
								+ "                                <img src=\"/applications/Mail_Interface/3_3/modules/User_Interface/core/v31_campaigns/images/neweditor/default/temp_img_1.png\" data-default=\"placeholder\" width='150' data-max-width=\"150\">\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='50'></td>\r\n"
								+ "                </tr>\r\n" + "            </table>\r\n" + "        </div>\r\n"
								+ "    </div>\r\n" + "\r\n"
								+ "    <div class=\"customZone\" data-type=\"textText\">\r\n"
								+ "        <div class=\"movableContent\">\r\n"
								+ "            <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width='600'>\r\n"
								+ "            	<tr><td height='30' colspan='5'></td></tr>\r\n"
								+ "                <tr>\r\n" + "                	<td width='50'></td>\r\n"
								+ "                    <td valign=\"top\" width=\"230\">\r\n"
								+ "                        <div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "                            <div class=\"contentEditable\" style=\"color:#555;font-family:Helvetica, Arial, sans-serif;font-size:16px;line-height:160%;\">\r\n"
								+ "                                <h2 >This is a subtitle</h2>\r\n"
								+ "                                <p >Etiam bibendum nunc in lacus bibendum porta. Vestibulum nec nulla et eros ornare condimentum. Proin facilisis, dui in mollis blandit. Sed non dui magna, quis tincidunt enim.</p>\r\n"
								+ "                                <p style=\"text-align:right;\"><a target='_blank' href=\"\">Read more</a></p>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='40'></td>\r\n"
								+ "                    <td valign=\"top\" width='230'>\r\n"
								+ "                        <div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "                            <div class=\"contentEditable\" style=\"color:#555;font-family:Helvetica, Arial, sans-serif;font-size:16px;line-height:160%;\">\r\n"
								+ "                                <h2 >This is a subtitle</h2>\r\n"
								+ "                                <p style=\"text-align:left;\">Etiam bibendum nunc in lacus bibendum porta. Vestibulum nec nulla et eros ornare condimentum. Proin facilisis, dui in mollis blandit. Sed non dui magna, quis tincidunt enim.</p>\r\n"
								+ "                                <p style=\"text-align:right;\"><a target='_blank' href=\"\">Read more</a></p>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='50'></td>\r\n"
								+ "                </tr>\r\n" + "            </table>\r\n" + "        </div>\r\n"
								+ "    </div>\r\n" + "\r\n" + "    <div class=\"customZone\" data-type=\"qrcode\">\r\n"
								+ "        <div class=\"movableContent\">\r\n"
								+ "            <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width='600'>\r\n"
								+ "            	<tr><td height='30' colspan='5'></td></tr>\r\n"
								+ "                <tr>\r\n" + "                	<td width='50'></td>\r\n"
								+ "                    <td valign=\"top\" >\r\n"
								+ "                        <div class=\"contentQrcodeEditable contentEditableContainer\">\r\n"
								+ "                            <div class=\"contentEditable\">\r\n"
								+ "                                <img src=\"/applications/Mail_Interface/3_3/modules/User_Interface/core/v31_campaigns/images/neweditor/default/qr_code.png\" width=\"75\" height=\"75\" data-default=\"placeholder\">\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='20'></td>\r\n"
								+ "                    <td valign=\"top\">\r\n"
								+ "                        <div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "                            <div class=\"contentEditable\" style=\"color:#555;font-family:Helvetica, Arial, sans-serif;font-size:16px;line-height:160%;\">\r\n"
								+ "                                <h2 >This is a subtitle</h2>\r\n"
								+ "                                <p style=\"text-align:left;\">Etiam bibendum nunc in lacus bibendum porta. Vestibulum nec nulla et eros ornare condimentum. Proin facilisis, dui in mollis blandit. Sed non dui magna, quis tincidunt enim. Morbi vehicula pharetra lacinia. Cras tincidunt, justo at fermentum feugiat, eros orci accumsan dolor, eu ultricies eros dolor quis sapien. Curabitur in turpis sem, a sodales purus. Pellentesque et risus at mauris aliquet gravida.</p>\r\n"
								+ "                                <p style=\"text-align:left;\">Integer in elit in tortor posuere molestie non a velit. Pellentesque consectetur, nisi a euismod scelerisque.</p>\r\n"
								+ "                                <p style=\"text-align:right;\"><a target='_blank' href=\"\">Read more</a></p>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='50'></td>\r\n"
								+ "                </tr>\r\n" + "            </table>\r\n" + "        </div>\r\n"
								+ "    </div>\r\n" + "\r\n" + "    <div class=\"customZone\" data-type=\"social\">\r\n"
								+ "        <div class=\"movableContent\" align='center'>\r\n"
								+ "            <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width='600'>\r\n"
								+ "            	<tr><td height='30' colspan='5'></td></tr>\r\n"
								+ "                <tr>\r\n" + "                	<td width='50'></td>\r\n"
								+ "                    <td valign=\"top\" width=\"230\" align='center'>\r\n"
								+ "                        <div class=\"contentEditableContainer contentFacebookEditable\">\r\n"
								+ "                            <div class=\"contentEditable\">\r\n"
								+ "                                <img data-default=\"placeholder\" src=\"images/facebook.png\" data-max-width='60' data-customIcon=\"true\" data-noText=\"true\" width='60' height='60'>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                        <div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "                            <div class=\"contentEditable\" style=\"color:#555;font-family:Helvetica, Arial, sans-serif;font-size:16px;line-height:160%;\">\r\n"
								+ "                                <h2 >Facebook</h2>\r\n"
								+ "                                <p>Like us on Facebook to keep up with our news, updates and other discussions.</p>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='40'></td>\r\n"
								+ "                    <td valign=\"top\" width=\"230\" align='center'>\r\n"
								+ "                        <div class=\"contentEditableContainer contentTwitterEditable\">\r\n"
								+ "                            <div class=\"contentEditable\">\r\n"
								+ "                                <img data-default=\"placeholder\" src=\"images/twitter.png\" data-max-width='60' data-customIcon=\"true\" data-noText=\"true\" width='60' height='60'>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                        <div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "                            <div class=\"contentEditable\" style=\"color:#555;font-family:Helvetica, Arial, sans-serif;font-size:16px;line-height:160%;\">\r\n"
								+ "                                <h2 >Twitter</h2>\r\n"
								+ "                                <p>Follow us on twitter to stay up to date with company news and other information.</p>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='50'></td>\r\n"
								+ "                </tr>\r\n" + "            </table>\r\n" + "        </div>\r\n"
								+ "    </div>\r\n" + "\r\n" + "    <div class=\"customZone\" data-type=\"twitter\">\r\n"
								+ "        <div class=\"movableContent\" align='center'>\r\n"
								+ "            <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width='600'>\r\n"
								+ "            	<tr><td height='30' colspan='3'></td></tr>\r\n"
								+ "                <tr>\r\n" + "                	<td width='50'></td>\r\n"
								+ "                    <td valign=\"top\" align='center'>\r\n"
								+ "                        <div class=\"contentEditableContainer contentTwitterEditable\">\r\n"
								+ "                            <div class=\"contentEditable\">\r\n"
								+ "                                <img data-default=\"placeholder\" src=\"images/twitter.png\" data-max-width='60' data-customIcon=\"true\" data-noText=\"true\" width='60' height='60'>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                        <div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "                            <div class=\"contentEditable\" style=\"color:#555;font-family:Helvetica, Arial, sans-serif;font-size:16px;line-height:160%;\">\r\n"
								+ "                                <h2 >Twitter</h2>\r\n"
								+ "                                <p>Follow us on twitter to stay up to date with company news and other information.</p>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='50'></td>\r\n"
								+ "                </tr>\r\n" + "            </table>\r\n" + "        </div>\r\n"
								+ "   </div>\r\n" + "\r\n"
								+ "    <div class=\"customZone\" data-type=\"facebook\" >\r\n"
								+ "        <div class=\"movableContent\" align='center'>\r\n"
								+ "            <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width='600'>\r\n"
								+ "            	<tr><td height='30' colspan='3'></td></tr>\r\n"
								+ "                <tr>\r\n" + "                	<td width='50'></td>\r\n"
								+ "                    <td valign=\"top\" align='center'>\r\n"
								+ "                        <div class=\"contentEditableContainer contentFacebookEditable\">\r\n"
								+ "                            <div class=\"contentEditable\">\r\n"
								+ "                                <img data-default=\"placeholder\" src=\"images/facebook.png\" data-max-width='60' data-customIcon=\"true\" data-noText=\"true\" width='60' height='60'>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                        <div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "                            <div class=\"contentEditable\" style=\"color:#555;font-family:Helvetica, Arial, sans-serif;font-size:16px;line-height:160%;\">\r\n"
								+ "                                <h2>Facebook</h2>\r\n"
								+ "                                <p>Like us on Facebook to keep up with our news, updates and other discussions.</p>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='50'></td>\r\n"
								+ "                </tr>\r\n" + "            </table>\r\n" + "        </div>\r\n"
								+ "    </div>\r\n" + "\r\n" + "    <div class=\"customZone\" data-type=\"gmap\">\r\n"
								+ "        <div class=\"movableContent\">\r\n"
								+ "            <table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width='600'>\r\n"
								+ "                <tr><td height='30' colspan='5'></td></tr>\r\n"
								+ "                <tr>\r\n" + "                	<td width='50'></td>\r\n"
								+ "                    <td valign=\"top\" >\r\n"
								+ "                        <div class=\"contentGmapEditable contentEditableContainer\">\r\n"
								+ "                            <div class=\"contentEditable\">\r\n"
								+ "                                <img src=\"/applications/Mail_Interface/3_3/modules/User_Interface/core/v31_campaigns/images/neweditor/default/gmap_example.png\" width=\"75\" data-default=\"placeholder\">\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='20'></td>\r\n"
								+ "                    <td valign=\"top\">\r\n"
								+ "                        <div class=\"contentEditableContainer contentTextEditable\">\r\n"
								+ "                            <div class=\"contentEditable\" style=\"color:#555;font-family:Helvetica, Arial, sans-serif;font-size:16px;line-height:160%;\">\r\n"
								+ "                                <h2 >This is a subtitle</h2>\r\n"
								+ "                                <p style=\"text-align:left;\">Etiam bibendum nunc in lacus bibendum porta. Vestibulum nec nulla et eros ornare condimentum. Proin facilisis, dui in mollis blandit. Sed non dui magna, quis tincidunt enim. Morbi vehicula pharetra lacinia. Cras tincidunt, justo at fermentum feugiat, eros orci accumsan dolor, eu ultricies eros dolor quis sapien. Curabitur in turpis sem, a sodales purus. Pellentesque et risus at mauris aliquet gravida.</p>\r\n"
								+ "                                <p style=\"text-align:left;\">Integer in elit in tortor posuere molestie non a velit. Pellentesque consectetur, nisi a euismod scelerisque.</p>\r\n"
								+ "                                <p style=\"text-align:right;\"><a target='_blank' href=\"\">Read more</a></p>\r\n"
								+ "                            </div>\r\n" + "                        </div>\r\n"
								+ "                    </td>\r\n" + "                    <td width='50'></td>\r\n"
								+ "                </tr>\r\n" + "            </table>\r\n" + "        </div>\r\n"
								+ "    </div>\r\n" + "\r\n" + "\r\n"
								+ "	 <div class=\"customZone\" data-type=\"colums1v2\"><div class='movableContent'>\r\n"
								+ "          	<table width=\"600\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\" >\r\n"
								+ "	            <tr><td height=\"30\" colspan='3'>&nbsp;</td></tr>\r\n"
								+ "	            <tr>\r\n" + "	            	<td width='50'></td>\r\n"
								+ "	              	<td width='500' align=\"center\" valign=\"top\" class=\"newcontent\">\r\n"
								+ "	                \r\n" + "	              	</td>\r\n"
								+ "	              	<td width='50'></td>\r\n" + "	            </tr>\r\n"
								+ "          	</table>\r\n" + "    	</div>\r\n" + "      </div>\r\n" + "\r\n"
								+ "      <div class=\"customZone\" data-type=\"colums2v2\"><div class='movableContent'>\r\n"
								+ "          <table width=\"600\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\" >\r\n"
								+ "	            <tr><td height=\"30\" colspan='3'>&nbsp;</td></tr>\r\n"
								+ "	            <tr>\r\n" + "	            	<td width='50'></td>\r\n"
								+ "	              	<td width='235' align=\"center\" valign=\"top\" class=\"newcontent\">\r\n"
								+ "	                \r\n" + "	              	</td>\r\n"
								+ "					<td width='30'></td>\r\n"
								+ "	              	<td width='235' align=\"center\" valign=\"top\" class=\"newcontent\">\r\n"
								+ "	                \r\n" + "	              	</td>\r\n"
								+ "	              	<td width='50'></td>\r\n" + "	            </tr>\r\n"
								+ "          	</table>\r\n" + "    	</div>\r\n" + "      </div>\r\n" + "\r\n"
								+ "      <div class=\"customZone\" data-type=\"colums3v2\"><div class='movableContent'>\r\n"
								+ "         <table width=\"600\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\" >\r\n"
								+ "	            <tr><td height=\"30\" colspan='3'>&nbsp;</td></tr>\r\n"
								+ "	            <tr>\r\n" + "	            	<td width='50'></td>\r\n"
								+ "	              	<td width='158' align=\"center\" valign=\"top\" class=\"newcontent\">\r\n"
								+ "	                \r\n" + "	              	</td>\r\n"
								+ "					<td width='12'></td>\r\n"
								+ "	              	<td width='158' align=\"center\" valign=\"top\" class=\"newcontent\">\r\n"
								+ "	                \r\n" + "	              	</td>\r\n"
								+ "					<td width='12'></td>\r\n"
								+ "	              	<td width='158' align=\"center\" valign=\"top\" class=\"newcontent\">\r\n"
								+ "	                \r\n" + "	              	</td>\r\n"
								+ "	              	<td width='50'></td>\r\n" + "	            </tr>\r\n"
								+ "          	</table>\r\n" + "    	</div>\r\n" + "      </div>\r\n" + "\r\n"
								+ "      <div class=\"customZone\" data-type=\"textv2\">\r\n"
								+ "		<div class=\"contentEditableContainer contentTextEditable\" >\r\n"
								+ "            <div class=\"contentEditable\" >\r\n" + "                \r\n"
								+ "				<h2 >Make sure you’re recognizable</h2>\r\n" + "            </div>\r\n"
								+ "        </div>\r\n"
								+ "		<div class=\"contentEditableContainer contentTextEditable\" >\r\n"
								+ "            <div class=\"contentEditable\" >\r\n"
								+ "				<p>Include both the name of the person who’s sending the email as well as the name of the company, and even better: send using your own domain.</p>\r\n"
								+ "            </div>\r\n" + "        </div>\r\n"
								+ "		<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\" width=\"79%\" height=\"50\">\r\n"
								+ "			<tr>\r\n"
								+ "				<td bgcolor=\"#ED006F\" align=\"center\" style=\"border-radius:4px;\" width=\"100%\" height=\"50\">\r\n"
								+ "					<div class=\"contentEditableContainer contentTextEditable\" >\r\n"
								+ "                        <div class=\"contentEditable\" style='text-align:center;'>\r\n"
								+ "                            <a target='_blank' href=\"[CLIENTS.WEBSITE]\" class='link2'>Read the 3 rules of email marketing sender etiquette</a>\r\n"
								+ "                        </div>\r\n" + "                    </div>\r\n"
								+ "				</td>\r\n" + "			</tr>\r\n" + "		</table>\r\n"
								+ "      </div>\r\n" + "\r\n" + "\r\n" + "\r\n" + "\r\n" + "-->\r\n"
								+ "<!--Default Zone End-->\r\n" + "\r\n" + "</body>\r\n" + "</html>",
						true);
				sender.send(msg);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ModelAndView verifyLink(String uniqueId, Long publisherId) {
		ModelAndView modelAndView = new ModelAndView();
		Publisher publisher = null;
		
		try {
			 publisher = publisherRepo.findById(publisherId).get();
		}catch (Exception e) {
			modelAndView.addObject("malicious","Malicious Activity Detected, Please Register again.");
			modelAndView.setViewName("registrationPage");
			publisherRepo.deleteById(publisherId);
			return modelAndView;
		}
		
		
		//does this link belongs to this publisher 
		if(!publisher.getUniqueId().equals(uniqueId)) {
			modelAndView.addObject("malicious","Malicious Activity Detected, Please Register again.");
			modelAndView.setViewName("registrationPage");
			publisherRepo.deleteById(publisherId);
		}else {
			//Is this link opened after 24hrs
			Date createdDate = publisher.getCreatedOn();
			Calendar calender = Calendar.getInstance();
			calender.setTime(createdDate);
			calender.add(Calendar.HOUR, 24);
			Date currentDt =  calender.getTime();
			
			if(new Date().before(currentDt)) {
				modelAndView.addObject("Success","Account activated successfully please login.");
				//activate the publisher
				publisherRepo.changeStatePublisher(true, publisherId);
				
//				authorize to the publisher
				Authorities authorities = new Authorities();
				authorities.setUsername(publisher.getUsername());
				authoritiesRepo.save(authorities);

				modelAndView.setViewName("login");
			}
			else 
			{
				modelAndView.addObject("LinkExpired","The link has been expired, Please Register again.");
				modelAndView.setViewName("registrationPage");
				publisherRepo.deleteById(publisherId);
			}
		}
		
		return modelAndView;
	}

}
