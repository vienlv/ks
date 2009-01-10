/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.faq.webui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.faq.webui.popup.UICategoryForm;
import org.exoplatform.faq.webui.popup.UIDeleteQuestion;
import org.exoplatform.faq.webui.popup.UIExportForm;
import org.exoplatform.faq.webui.popup.UIImportForm;
import org.exoplatform.faq.webui.popup.UIMoveCategoryForm;
import org.exoplatform.faq.webui.popup.UIMoveQuestionForm;
import org.exoplatform.faq.webui.popup.UIPopupAction;
import org.exoplatform.faq.webui.popup.UIPopupContainer;
import org.exoplatform.faq.webui.popup.UIQuestionForm;
import org.exoplatform.faq.webui.popup.UIQuestionManagerForm;
import org.exoplatform.faq.webui.popup.UIResponseForm;
import org.exoplatform.faq.webui.popup.UISendMailForm;
import org.exoplatform.faq.webui.popup.UISettingForm;
import org.exoplatform.faq.webui.popup.UIWatchForm;
import org.exoplatform.faq.webui.popup.UIWatchManager;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@SuppressWarnings("unused")
@ComponentConfig(
		template =	"app:/templates/faq/webui/UIQuestions.gtmpl" ,
		events = {
				@EventConfig(listeners = UIQuestions.DownloadAttachActionListener.class),
				@EventConfig(listeners = UIQuestions.AddCategoryActionListener.class),
				@EventConfig(listeners = UIQuestions.AddNewQuestionActionListener.class),
				@EventConfig(listeners = UIQuestions.OpenCategoryActionListener.class),
				@EventConfig(listeners = UIQuestions.EditSubCategoryActionListener.class),
				@EventConfig(listeners = UIQuestions.EditCategoryActionListener.class),
				@EventConfig(listeners = UIQuestions.DeleteCategoryActionListener.class, confirm= "UIQuestions.msg.confirm-delete-category"),
				@EventConfig(listeners = UIQuestions.MoveCategoryActionListener.class),
				@EventConfig(listeners = UIQuestions.MoveDownActionListener.class),
				@EventConfig(listeners = UIQuestions.MoveUpActionListener.class),
				@EventConfig(listeners = UIQuestions.SettingActionListener.class),
				@EventConfig(listeners = UIQuestions.WatchActionListener.class),
				@EventConfig(listeners = UIQuestions.WatchManagerActionListener.class),
				
				@EventConfig(listeners = UIQuestions.ExportActionListener.class),
				@EventConfig(listeners = UIQuestions.ImportActionListener.class),
				//@EventConfig(listeners = UIQuestions.ImportrActionListener.class),
				// action of question:
				@EventConfig(listeners = UIQuestions.QuestionManagamentActionListener.class),
				@EventConfig(listeners = UIQuestions.ViewQuestionActionListener.class),
				@EventConfig(listeners = UIQuestions.ResponseQuestionActionListener.class),
				@EventConfig(listeners = UIQuestions.EditQuestionActionListener.class),
				@EventConfig(listeners = UIQuestions.DeleteQuestionActionListener.class),
				@EventConfig(listeners = UIQuestions.MoveQuestionActionListener.class),
				@EventConfig(listeners = UIQuestions.SendQuestionActionListener.class),
				@EventConfig(listeners = UIQuestions.ChangeQuestionActionListener.class)

		}
)
public class UIQuestions extends UIContainer {
	private static String SEARCH_INPUT = "SearchInput" ;
	private static final String OBJECT_ITERATOR = "object_iter";
	private static final String QUICK_SEARCH = "QuickSearch";

	private FAQSetting faqSetting_ = null;
	private List<Category> categories_ = null ;
	private List<Boolean> categoryModerators = new ArrayList<Boolean>() ;
	public List<Question> listQuestion_ =  null ;
	private List<String> listCateId_ = new ArrayList<String>() ;
	private boolean canEditQuestion = false ;
	private String categoryId_ = null ;
	public String questionView_ = "" ;
	public static String newPath_ = "" ;
	private String currentUser_ = "";
	private List<String> listUserGroupMember = new ArrayList<String>();
	private String link_ ="";
	private static	FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
	public List<QuestionLanguage> listQuestionLanguage = new ArrayList<QuestionLanguage>() ;
	public boolean isChangeLanguage = false ;
	public List<String> listLanguage = new ArrayList<String>() ;
	public String backPath_ = "" ;
	private static String language_ = "" ;
	private List<Watch> watchList_ = new ArrayList<Watch>() ;

	private String[] firstTollbar_ = new String[]{"AddCategory", "AddNewQuestion", "QuestionManagament", "Export", "Import"} ;
	private String[] secondTollbar_ = new String[]{"AddCategory", "AddNewQuestion", "QuestionManagament", "Export", "Import"} ;
	private String[] firstActionCate_ = new String[]{"Export", "Import", "AddCategory", "AddNewQuestion", "EditCategory", "DeleteCategory", "MoveCategory", "MoveDown", "MoveUp", "Watch"} ;
	private String[] secondActionCate_ = new String[]{"Export", "Import", "AddCategory", "AddNewQuestion", "EditSubCategory", "DeleteCategory", "MoveCategory", "MoveDown", "MoveUp", "Watch"} ;
	private String[] userActionsCate_ = new String[]{"AddNewQuestion", "Watch"} ;
	private String[] moderatorActionQues_ = new String[]{"ResponseQuestion", "EditQuestion", "DeleteQuestion", "MoveQuestion", "SendQuestion"} ;
	private String[] userActionQues_ = new String[]{"SendQuestion"} ;
	private String[] sizes_ = new String[]{"bytes", "KB", "MB"};
	private boolean viewAuthorInfor = false;

	private String[] subCategoryAction = new String[]{"Export", "Import", "AddCategory", "AddNewQuestion", 
																										"DeleteCategory", "MoveCategory", "Watch"};
	
	public JCRPageList pageList ;
	private UIFAQPageIterator pageIterator = null ;
	long pageSelect = 0;

	public UIQuestions()throws Exception {
		backPath_ = null ;
		this.categoryId_ = new String() ;
		currentUser_ = FAQUtils.getCurrentUser() ;
		faqSetting_ = new FAQSetting();
		FAQUtils.getPorletPreference(faqSetting_);
		if(currentUser_ != null && currentUser_.trim().length() > 0){
			if(faqSetting_.getIsAdmin() == null || faqSetting_.getIsAdmin().trim().length() < 1){
				if(faqService_.isAdminRole(currentUser_)) faqSetting_.setIsAdmin("TRUE");
				else faqSetting_.setIsAdmin("FALSE");
			}
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			faqService_.getUserSetting(sessionProvider, currentUser_, faqSetting_);
			sessionProvider.close();
		} else {
			faqSetting_.setIsAdmin("FALSE");
		}
		addChild(UIQuickSearch.class, null, QUICK_SEARCH) ;
		addChild(UIFAQPageIterator.class, null, OBJECT_ITERATOR);
		setListObject();
	}

	public String getPortalName() {
		PortalContainer pcontainer =  PortalContainer.getInstance() ;
		return pcontainer.getPortalContainerInfo().getContainerName() ;  
	}

	public String getRepository() throws Exception {
		RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
		return rService.getCurrentRepository().getConfiguration().getName() ;
	}

	public void setListObject(){
		this.isChangeLanguage = false;
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		try {
			if(currentUser_ != null && currentUser_.trim().length() > 0){
				FAQServiceUtils serviceUtils = new FAQServiceUtils();
				if(faqSetting_.getIsAdmin().equals("TRUE")){
					faqSetting_.setCanEdit(true);
				} else if(categoryId_ != null && categoryId_.trim().length() > 0 &&
						Arrays.asList(faqService_.getCategoryById(this.categoryId_, sessionProvider).getModerators()).contains(currentUser_)){
					faqSetting_.setCanEdit(true);
				} else {
					faqSetting_.setCanEdit(false);
				}
			}
			String object = null;
			if(pageList != null) object = pageList.getObjectRepare_();
			pageList = faqService_.getListCatesAndQuesByCateId(this.categoryId_, sessionProvider, this.faqSetting_);
			pageList.setPageSize(10);
			if(object != null && object.trim().length() > 0) pageList.setObjectRepare_(object);
			pageIterator = this.getChildById(OBJECT_ITERATOR);
			pageIterator.updatePageList(pageList);
		} catch (Exception e) {
			this.pageList = null ;
			this.pageList.setPageSize(10);
			pageIterator.updatePageList(this.pageList) ;
			e.printStackTrace();
		} finally {
			sessionProvider.close();
		}
	}

	public String[] getActionTollbar() {
		if(this.categoryId_ == null || this.categoryId_.equals("null") || this.categoryId_.trim().length() < 1){
			return firstTollbar_;
		}
		return secondTollbar_ ;
	}

	public FAQSetting getFAQSetting(){
		return faqSetting_;
	}

	public void setFAQSetting(FAQSetting setting){
		this.faqSetting_ = setting;
		if(faqSetting_.isAdmin()){
			try {
				listUserGroupMember = FAQServiceUtils.getAllGroupAndMembershipOfUser(currentUser_);
			} catch (Exception e) {			}
		}
	}

	@SuppressWarnings("unused")
	private String[] getActionCategory(){
		return firstActionCate_ ;
	}

	private String[] getSecondActionCategory() {
		return secondActionCate_ ;
	}

	private String[] getActionCategoryWithUser() {
		return userActionsCate_ ;
	}


	@SuppressWarnings("unused")
	private String[] getActionQuestion(){
		if(canEditQuestion) {
			return moderatorActionQues_;
		} else {
			return userActionQues_;
		}
	}

	@SuppressWarnings("unused")
	private String[] getActionQuestionWithUser(){
		String[] action = new String[]{"SendQuestion"} ;
		return action ;
	}

	public void setCategories() throws Exception  {
		if(categories_ != null)
			categories_.clear() ;
		pageSelect = pageIterator.getPageSelected() ;
		categories_ = new ArrayList<Category>();
		listQuestion_ = new ArrayList<Question>();
		List<Object> listObject = this.pageList.getPageListCategoriesQuestions(pageSelect, null);
		for(Object obj : listObject){
			if(obj instanceof Category){
				categories_.add((Category)obj);
			} else {
				listQuestion_.add((Question)obj);
			}
		}
		while(listObject.isEmpty() && pageSelect > 1) {
			listObject = this.pageList.getPageListCategoriesQuestions(--pageSelect, null);
			for(Object obj : listObject){
				if(obj instanceof Category){
					categories_.add((Category)obj);
				} else {
					listQuestion_.add((Question)obj);
				}
			}
			pageIterator.setSelectPage(pageSelect) ;
		}
		pageIterator.setSelectPage(pageList.getPageJump());
		setIsModerators() ;
	}

	@SuppressWarnings("static-access")
	public void setLanguageView(String language){
		this.language_ = language;
	}

	public void setCategories(String categoryId) throws Exception  {
		setCategoryId(categoryId) ;
	}
	
	private String[] getMenuSubCategory(Category category){
		if(currentUser_ == null) return userActionsCate_;
		if(category.getModerators().length == 1 && currentUser_.equals(category.getModerators()[0])) return subCategoryAction;
		for(String str : category.getModerators()){
			if(listUserGroupMember.contains(str)) return subCategoryAction;
		}
		return userActionsCate_;
	}

	private void setIsModerators() {
		categoryModerators.clear() ;
		FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
		if(faqSetting_.getIsAdmin().equals("TRUE")) {
			canEditQuestion = true ;
			for(int i = 0 ; i < this.categories_.size(); i ++) {
				this.categoryModerators.add(true);
			}
		} else {
			if(categoryId_ == null || categoryId_.trim().length() < 1) {
				listCateId_.clear() ;
			} else {
				if(!listCateId_.contains(categoryId_)) {
					listCateId_.add(categoryId_) ;
				} else {
					int pos = listCateId_.indexOf(categoryId_) ;
					for(int i = pos + 1; i < listCateId_.size() ; i ++) {
						listCateId_.remove(i) ;
					}
				}
			}
			boolean isContinue = true ;
			if(listCateId_.size() > 0){
				SessionProvider sessionProvider = FAQUtils.getSystemProvider();
				for(String cateIdProcess : listCateId_) {
					try {
						if(Arrays.asList(faqService_.getCategoryById(cateIdProcess, sessionProvider).getModeratorsCategory()).contains(currentUser_)){
							for(int j = 0 ; j < categories_.size(); j ++) {
								categoryModerators.add(true) ;
							}
							isContinue = false ;
							canEditQuestion = true ;
							break ;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				sessionProvider.close();
			}

			if(isContinue) {
				canEditQuestion = false ;
				for(Category category : categories_) {
					try {
						if(Arrays.asList(category.getModeratorsCategory()).contains(currentUser_)) {
							categoryModerators.add(true) ;
						}else {
							categoryModerators.add(false) ;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	private List<Boolean> getIsModerator() {
		return this.categoryModerators ;
	}

	private String[] getActionWithCategory() {
		return null ;
	}

	@SuppressWarnings("unused")
	private List<Category> getCategories() throws Exception {
		if(isChangeLanguage && pageSelect != pageIterator.getPageSelected()) isChangeLanguage = false;
		if(!isChangeLanguage){
			setCategories();
		}
		return categories_ ;
	}

	@SuppressWarnings("unused")
	private long[] getCategoryInfo(String categoryId) {
		long[] result = new long[]{0, 0, 0, 0} ;
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		try {
			result = faqService_.getCategoryInfo(categoryId, sessionProvider) ;
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		sessionProvider.close();
		return result ;
	}

	public void setIsNotChangeLanguage() {
		isChangeLanguage = false;
	}

	public void setListQuestion(List<Question> listQuestion) {
		this.listQuestion_ = listQuestion ;
	}

	private String convertSize(long size){
		String result = "";
		long  residual = 0;
		int i = 0;
		while(size >= 1000){
			i ++;
			residual = size % 1024;
			size /= 1024;
		}
		if(residual > 500){
			String str = residual + "";
			result = (size + 1) + " " + sizes_[i];
		}else{
			result = size + " " + sizes_[i];
		}
		return result;
	}

	@SuppressWarnings("unused")
	public List<Question> getListQuestion() {
		return this.listQuestion_ ;
	}

	private boolean getCanEditQuestion() {
		return this.canEditQuestion ;
	}

	@SuppressWarnings("unused")
	private String getQuestionView(){
		return this.questionView_ ;
	}

	private List<String> getQuestionLangauges(Question question){
		try {
			if(!isChangeLanguage) {
				listLanguage.clear() ;
				listQuestionLanguage.clear() ;

				QuestionLanguage quesLanguage = new QuestionLanguage() ;
				quesLanguage.setLanguage(question.getLanguage()) ;
				quesLanguage.setQuestion(question.getQuestion()) ;
				quesLanguage.setResponse(question.getAllResponses()) ;
				quesLanguage.setResponseBy(question.getResponseBy());
				quesLanguage.setDateResponse(question.getDateResponse());
				listQuestionLanguage.add(quesLanguage) ;
				SessionProvider sessionProvider = FAQUtils.getSystemProvider();
				listQuestionLanguage.addAll(faqService_.getQuestionLanguages(question.getId(), sessionProvider)) ;
				for(QuestionLanguage questionLanguage : listQuestionLanguage) {
					listLanguage.add(questionLanguage.getLanguage()) ;
				}
				sessionProvider.close();
			}
			return listLanguage ;
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		return null ;
	}

	@SuppressWarnings("unused")  
	private String getFileSource(InputStream input, String fileName, DownloadService dservice) throws Exception {
		byte[] imageBytes = null;
		if (input != null) {
			imageBytes = new byte[input.available()];
			input.read(imageBytes);
			ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes);
			InputStreamDownloadResource dresource = new InputStreamDownloadResource(byteImage, "image");
			dresource.setDownloadName(fileName);
			return dservice.getDownloadLink(dservice.addDownloadResource(dresource));
		}
		return null;
	}

	private String getFileSource(FileAttachment attachment) throws Exception {
		DownloadService dservice = getApplicationComponent(DownloadService.class) ;
		try {
			InputStream input = attachment.getInputStream() ;
			String fileName = attachment.getName() ;
			return getFileSource(input, fileName, dservice);
		} catch (Exception e) {
			e.printStackTrace() ;
			return null;
		}
	}

	public void setQuestionView(String questionid){
		this.questionView_ = questionid ;
	}

	public String getCategoryId(){
		return this.categoryId_ ;
	}

	public void setCategoryId(String categoryId)  throws Exception {
		this.categoryId_ = categoryId ;
		setListObject();
		setIsNotChangeLanguage();
	}

	public String getQuestionRelationById(String questionId) {
		Question question = new Question();
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		try {
			question = faqService_.getQuestionById(questionId, sessionProvider);
			if(question != null) {
				return question.getCategoryId() + "/" + question.getId() + "/" + question.getQuestion();
			} else {
				return "" ;
			}
		} catch (Exception e) {
			e.printStackTrace();
			sessionProvider.close();
			return "" ;
		} finally {
			sessionProvider.close();
		}
	}

	public void moveDownUp(Event<UIQuestions> event, int i) {
		String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
		int index = 0 ;
		for (Category cate : categories_) {
			if (cate.getId().equals(categoryId)) {
				break ;
			} else {
				index ++ ;
			}
		}

		if (index < 0) return ;
		if ( index ==0 && i == -1) return ;
		if (index == categories_.size()-1 && i==1) return ;
		Category category = categories_.remove(index) ;
		for (Category cate : categories_) {
		}
		categories_.add(index+i, category) ;
	}	

	private String getBackPath() {
		return this.backPath_ ;
	}

	public List<Watch> getListWatch(String categoryId) throws Exception {
		FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		watchList_ = faqService.getListMailInWatch(categoryId, sessionProvider) ;
		sessionProvider.close();
		return watchList_ ;
	}

	public void setListWatch(List<Watch> list) { watchList_ = list ;}

	public void setPath(String s) { newPath_ = s ; }

	public String getPathService(String categoryId) throws Exception {
		String oldPath = "";
		String path = "FAQService";
		if(categoryId != null && !categoryId.equals("null")){
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			List<String> listPath = faqService_.getCategoryPath(sessionProvider, categoryId) ;
			sessionProvider.close();
			for(int i = listPath.size() -1 ; i >= 0; i --) {
				oldPath = oldPath + "/" + listPath.get(i);
			}
			path += oldPath ;
			oldPath = path.substring(0, path.lastIndexOf("/")) ;
		} else {
			oldPath = path;
		}
		return oldPath ;
	}

	public String cutCaret(String name) {
		StringBuffer string = new StringBuffer();
		char c;
		for (int i = 0; i < name.length(); i++) {
			c = name.charAt(i) ;
			if(c == 47) continue ;
			string.append(c) ;
		}
		return string.toString();
	}

	public String getLink() {return link_;}
	public void setLink(String link) { this.link_ = link;}
	
	// get all subcategory to fix for issue ks-768: List questions under categories
	private List<Category> getSubCategories(String cateId){
		SessionProvider sProvider = FAQUtils.getSystemProvider();
		List<Category> listCategories = new ArrayList<Category>();
		try {
			listCategories = faqService_.getSubCategories(cateId, sProvider, faqSetting_);
		} catch (Exception e) {
			e.printStackTrace();
		}
		sProvider.close();
		return listCategories;
	}
	//get all sub Questions to fix for issue ks-768: List questions under categories
	private List<Question> getListSubQuestion(String cateId){
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		List<Question> listQuestions = new ArrayList<Question>();
		try {
			listQuestions = faqService_.getQuestionsByCatetory(cateId, sessionProvider, faqSetting_).getAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		sessionProvider.close();
		return listQuestions;
	}

	// get all sub category to delete all question of these category
	private List<Category> getAllSubCategory(String categoryId) throws Exception {
		List<Category> listResult = new ArrayList<Category>() ;
		Stack<Category> stackCate = new Stack<Category>() ;
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		Category cate = null ;
		listResult.add(faqService_.getCategoryById(categoryId, sessionProvider)) ;
		for(Category category : faqService_.getSubCategories(categoryId, sessionProvider, this.faqSetting_)) {
			stackCate.push(category) ;
		}
		while(!stackCate.isEmpty()) {
			cate = stackCate.pop() ;
			listResult.add(cate) ;
			for(Category category : faqService_.getSubCategories(cate.getId(), sessionProvider, this.faqSetting_)) {
				stackCate.push(category) ;
			}
		}
		sessionProvider.close();
		return listResult ;
	}

	static public class DownloadAttachActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			event.getRequestContext().addUIComponentToUpdateByAjax(question) ;
		}
	}

	static  public class AddCategoryActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ; 
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			UIPopupContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UICategoryForm category = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
			if(!FAQUtils.isFieldEmpty(categoryId)) {
				SessionProvider sessionProvider = FAQUtils.getSystemProvider();
				try {
					Category cate = faqService_.getCategoryById(categoryId, sessionProvider) ;
					String moderator[] = cate.getModeratorsCategory() ;
					String currentUser = FAQUtils.getCurrentUser() ;
					FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
					if(Arrays.asList(moderator).contains(currentUser)|| question.faqSetting_.isCanEdit() || question.canEditQuestion) {
						uiPopupAction.activate(uiPopupContainer, 540, 320) ;
						uiPopupContainer.setId("SubCategoryForm") ;
						category.setParentId(categoryId) ;
					} else {
						uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
						question.setIsNotChangeLanguage();
						event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
						return ;
					}
				} catch (Exception e) {
					e.printStackTrace();
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
					sessionProvider.close();
					return ;
				} finally {
					sessionProvider.close();
				}
			} else {
				uiPopupAction.activate(uiPopupContainer, 540, 320) ;
				uiPopupContainer.setId("AddCategoryForm") ;
			}
			category.init(true) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
			UIFAQContainer fAQContainer = question.getAncestorOfType(UIFAQContainer.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
		}
	}

	static public class AddNewQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			questions.isChangeLanguage = false ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			if(categoryId != null){
				try {
					faqService_.getCategoryById(categoryId, sessionProvider);
				} catch (Exception e) {
					UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					try {
						questions.setCategories() ;
					} catch (Exception pathEx){
						UIFAQContainer container = questions.getParent() ;
						UIBreadcumbs breadcumbs = container.findFirstComponentOfType(UIBreadcumbs.class) ;
						String pathCate = "" ;
						for(String path : breadcumbs.paths_.get(breadcumbs.paths_.size() - 1).split("/")) {
							if(path.equals("FAQService")){
								pathCate = path ;
								continue ;
							}
							try {
								faqService_.getCategoryById(path, sessionProvider);
								if(pathCate.trim().length() > 0) pathCate += "/" ;
								pathCate += path ;
							} catch (Exception pathExc) {
								try {
									breadcumbs.setUpdataPath(pathCate) ;
								} catch (Exception exc) {
									e.printStackTrace();
								}
								if(pathCate.indexOf("/") > 0) {
									questions.setCategoryId(pathCate.substring(pathCate.lastIndexOf("/") + 1)) ;
									event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
								} else {
									questions.categoryId_ = null ;
									//questions.setCategories() ;
									questions.setListObject();
									questions.setIsNotChangeLanguage() ;
									event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
								}
								break ;
							}
						}
					}
					UIFAQContainer fAQContainer = questions.getAncestorOfType(UIFAQContainer.class) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
					sessionProvider.close();
					return ;
				}
			}
			sessionProvider.close();
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIQuestionForm questionForm = popupContainer.addChild(UIQuestionForm.class, null, null) ;
			String email = "" ;
			String name = "" ;
			String userName = FAQUtils.getCurrentUser() ;
			if(!FAQUtils.isFieldEmpty(userName)){
				name = FAQUtils.getFullName(userName) ;
				email = FAQUtils.getEmailUser(userName) ;
			}
			questionForm.setFAQSetting(questions.faqSetting_) ;
			questionForm.setAuthor(name) ;
			questionForm.setEmail(email) ;
			questionForm.setCategoryId(categoryId) ;
			questionForm.refresh() ;
			popupContainer.setId("AddQuestion") ;
			popupAction.activate(popupContainer, 600, 400) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static public class OpenCategoryActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			questions.pageSelect = 0;
			questions.backPath_ = "" ;
			UIFAQPortlet faqPortlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			String parentId = null;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			if(categoryId.indexOf("/") > 0){
				String[] ids = categoryId.split("/");
				parentId = ids[0];
				categoryId = ids[1];
			}
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try {
				questions.viewAuthorInfor = faqService_.getCategoryById(categoryId, sessionProvider).isViewAuthorInfor() ;
			} catch (Exception e) {
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				questions.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
				sessionProvider.close();
				return ;
			}
			questions.setCategoryId(categoryId) ;
			UIBreadcumbs breadcumbs = faqPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
			String oldPath = breadcumbs.getPaths() ;
			if(parentId != null) oldPath += "/" + parentId;
			if(oldPath != null && oldPath.trim().length() > 0) {
				if(!oldPath.contains(categoryId)) {
					newPath_ = oldPath + "/" +categoryId ;
					questions.setPath(newPath_) ;
					breadcumbs.setUpdataPath(oldPath + "/" +categoryId);
				}
			} else breadcumbs.setUpdataPath(categoryId);
			sessionProvider.close();
			event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
			UIFAQContainer fAQContainer = questions.getAncestorOfType(UIFAQContainer.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
		}
	}

	static	public class EditCategoryActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try {
				Category cate = faqService_.getCategoryById(categoryId, sessionProvider) ;
				String moderator[] = cate.getModeratorsCategory() ;
				String currentUser = FAQUtils.getCurrentUser() ;
				FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
				if(Arrays.asList(moderator).contains(currentUser)|| question.faqSetting_.isAdmin()) {
					UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class,540) ;
					uiPopupContainer.setId("EditCategoryForm") ;
					UICategoryForm uiCategoryForm = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
					uiCategoryForm.init(false);
					uiCategoryForm.setCategoryValue(categoryId, true) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
					sessionProvider.close();
					return ;
				}
			} catch (Exception e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				question.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
				sessionProvider.close();
				return ;
			} finally {
				sessionProvider.close();
			}
		}
	}

	static	public class DeleteCategoryActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 			
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIApplication uiApp = question.getAncestorOfType(UIApplication.class) ;
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try {
				Category cate = faqService_.getCategoryById(categoryId, sessionProvider) ;
				String moderator[] = cate.getModeratorsCategory() ;
				String currentUser = FAQUtils.getCurrentUser() ;
				FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
				if(Arrays.asList(moderator).contains(currentUser)|| question.faqSetting_.isAdmin()) {
					List<Category> listCate = question.getAllSubCategory(categoryId) ;
					FAQSetting faqSetting = new FAQSetting();
					faqSetting.setDisplayMode(FAQUtils.DISPLAYBOTH);
					faqSetting.setOrderBy("alphabet");
					faqSetting.setOrderType("asc");
					for(Category category : listCate) {
						String id = category.getId() ;
						List<Question> listQuestion = faqService_.getAllQuestionsByCatetory(id, sessionProvider, faqSetting).getAll() ;
						for(Question ques: listQuestion) {
							String questionId = ques.getId() ;
							faqService_.removeQuestion(questionId, sessionProvider) ;
						}
					}
					faqService_.removeCategory(categoryId, sessionProvider) ;
					question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
					sessionProvider.close();
					return ;
				}
			} catch (Exception e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				question.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
				sessionProvider.close();
				return ;
			} finally {
				sessionProvider.close();
			}
		}
	}

	static	public class MoveCategoryActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet faqPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = faqPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try {
				Category cate = faqService_.getCategoryById(categoryId, sessionProvider) ;
				String moderator[] = cate.getModeratorsCategory() ;
				String currentUser = FAQUtils.getCurrentUser() ;
				FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
				if(Arrays.asList(moderator).contains(currentUser)|| question.faqSetting_.isAdmin()) {
					List<Category> listCate = faqService_.getSubCategories(null, sessionProvider, question.faqSetting_) ;
					String cateId = null ;
					if(listCate.size() == 1 ) {
						for(Category cat: listCate) { cateId = cat.getId(); }
					} 
					if(listCate.size() > 1 || listCate.size() == 1 && !categoryId.equals(cateId)) {
						UIMoveCategoryForm uiMoveCategoryForm = popupContainer.addChild(UIMoveCategoryForm.class, null, null) ;
						popupContainer.setId("MoveCategoryForm") ;
						uiMoveCategoryForm.setCategoryID(categoryId) ;
						uiMoveCategoryForm.setFAQSetting(question.faqSetting_) ;
						uiMoveCategoryForm.setListCate() ;
						popupAction.activate(popupContainer, 600, 400) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
					} else {
						uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.cannot-move-category", null, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
						question.setIsNotChangeLanguage();
						event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
					}
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
				}
			} catch (Exception e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				question.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
			} finally {
				sessionProvider.close();
			}
		}
	}

	static	public class MoveUpActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			UIFAQPortlet faqPortlet = question.getAncestorOfType(UIFAQPortlet.class) ;
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try {
				Category cate = faqService_.getCategoryById(categoryId, sessionProvider) ;
				String moderator[] = cate.getModeratorsCategory() ;
				String currentUser = FAQUtils.getCurrentUser() ;
				FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
				if(Arrays.asList(moderator).contains(currentUser)|| question.faqSetting_.isAdmin()) {
					question.moveDownUp(event, -1);
					question.isChangeLanguage = true;
					event.getRequestContext().addUIComponentToUpdateByAjax(question) ;
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
				}
			} catch (Exception e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				question.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
			} finally {
				sessionProvider.close();
			}
		}
	}

	static	public class MoveDownActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			UIFAQPortlet faqPortlet = question.getAncestorOfType(UIFAQPortlet.class) ;
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try {
				Category cate = faqService_.getCategoryById(categoryId, sessionProvider) ;
				String moderator[] = cate.getModeratorsCategory() ;
				String currentUser = FAQUtils.getCurrentUser() ;
				FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
				if(Arrays.asList(moderator).contains(currentUser)|| question.faqSetting_.isAdmin()) {
					question.moveDownUp(event, 1);
					question.isChangeLanguage = true;
					event.getRequestContext().addUIComponentToUpdateByAjax(question) ;
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
				}
			} catch (Exception e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				question.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
			} finally {
				sessionProvider.close();
			}
		}
	}

	static	public class SettingActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			String cateId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;	
			UISettingForm uiSetting = popupAction.activate(UISettingForm.class, 400) ;
			popupContainer.setId("CategorySettingForm") ;
			uiSetting.setFaqSetting(question.faqSetting_);
			uiSetting.init() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static	public class WatchActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ;
			String cateId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try {
				faqService_.getCategoryById(cateId, sessionProvider) ;
			} catch (Exception e) {
				UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				question.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
				sessionProvider.close();
				return ;
			} finally {
				sessionProvider.close();
			}
			
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIWatchForm uiWatchForm = popupAction.activate(UIWatchForm.class, 420) ;
			popupContainer.setId("CategoryWatchForm") ;
			uiWatchForm.setCategoryID(cateId) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static	public class WatchManagerActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ;
			String cateId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet faqPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = faqPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try {
				Category cate = faqService_.getCategoryById(cateId, sessionProvider) ;
				String moderator[] = cate.getModeratorsCategory() ;
				String currentUser = FAQUtils.getCurrentUser() ;
				FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
				if(Arrays.asList(moderator).contains(currentUser)|| question.faqSetting_.isAdmin()) {
					UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
					UIWatchContainer watchContainer = popupAction.activate(UIWatchContainer.class, 600) ;
					UIWatchManager watchManager = watchContainer.getChild(UIWatchManager.class) ;
					popupContainer.setId("WatchManager") ;
					watchManager.setCategoryID(cateId) ;
					List<Watch> emailWatch = faqService_.getListMailInWatch(cateId, sessionProvider) ;
					watchManager.setListWatch(emailWatch) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
				}
			} catch (Exception e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				question.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
			} finally {
				sessionProvider.close();
			}
		}
	}

	static  public class EditSubCategoryActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet faqPortlet = questions.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction uiPopupAction = faqPortlet.getChild(UIPopupAction.class) ; 
			UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try {
				String newPath = questions.cutCaret(newPath_) ;
				String pathService = questions.cutCaret(questions.getPathService(categoryId)) ;
				Category cate = faqService_.getCategoryById(categoryId, sessionProvider) ;
				String moderator[] = cate.getModeratorsCategory() ;
				String currentUser = FAQUtils.getCurrentUser() ;
				FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
				if(Arrays.asList(moderator).contains(currentUser) || questions.faqSetting_.isAdmin()) {
					if (newPath.equals(pathService)) {
						UIPopupContainer uiPopupContainer = uiPopupAction.activate(UIPopupContainer.class,550) ;  
						uiPopupContainer.setId("EditSubCategoryForm") ;
						UICategoryForm categoryForm = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
						categoryForm.init(false) ;
						String parentCategoryId = newPath_.substring(newPath_.lastIndexOf("/")+1, newPath_.length()) ;
						categoryForm.setParentId(parentCategoryId) ;
						categoryForm.setCategoryValue(categoryId, true) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
					} else {
						uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-moved-action", null, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
						questions.setIsNotChangeLanguage();
						event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
					}
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					questions.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
				}
			} catch (Exception e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				questions.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
			} finally {
				sessionProvider.close();
			}
		}
	}

	static  public class QuestionManagamentActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			
			UIQuestionManagerForm questionManagerForm = popupContainer.addChild(UIQuestionManagerForm.class, null, null) ;
			popupContainer.setId("FAQQuestionManagerment") ;
			popupAction.activate(popupContainer, 900, 850) ;
			questionManagerForm.setFAQSetting(questions.faqSetting_);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	// action for question :

	static  public class ExportActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.setId("FAQExportForm") ;
			UIExportForm exportForm = popupContainer.addChild(UIExportForm.class, null, null) ;
			popupAction.activate(popupContainer, 500, 200) ;
			exportForm.setObjectId(categoryId);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static  public class ImportActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.setId("FAQImportForm") ;
			UIImportForm importForm = popupContainer.addChild(UIImportForm.class, null, null) ;
			popupAction.activate(popupContainer, 500, 170) ;
			importForm.setCategoryId(categoryId);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static  public class ViewQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ;
			UIFAQPortlet faqPortlet = uiQuestions.getAncestorOfType(UIFAQPortlet.class) ;
			UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
			uiQuestions.isChangeLanguage = false ;
			String strId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			String questionId = new String() ;
			Question question = new Question();
			language_ = "" ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try{
				if(strId.indexOf("/") < 0) {
					questionId = strId ;
					question = faqService_.getQuestionById(questionId, sessionProvider) ;
					uiQuestions.backPath_ = "" ;
					for(int i = 0; i < uiQuestions.listQuestion_.size(); i ++) {
						if(uiQuestions.listQuestion_.get(i).getId().equals(uiQuestions.questionView_)) {
							uiQuestions.listQuestion_.get(i).setQuestion(uiQuestions.listQuestionLanguage.get(0).getQuestion()) ;
							uiQuestions.listQuestion_.get(i).setLanguage(uiQuestions.listQuestionLanguage.get(0).getLanguage()) ;
							uiQuestions.listQuestion_.get(i).setResponses(uiQuestions.listQuestionLanguage.get(0).getResponse()) ;
							uiQuestions.listQuestion_.get(i).setDateResponse(uiQuestions.listQuestionLanguage.get(0).getDateResponse());
							break ;
						}
					}
				} else {
					String[] strArr = strId.split("/");
					if((uiQuestions.backPath_ != null && uiQuestions.backPath_.trim().length() > 0 && uiQuestions.backPath_.equals(strId))
							|| strArr.length == 3) {
						uiQuestions.backPath_ = "" ;
					} else {
						uiQuestions.backPath_ = uiQuestions.categoryId_ + "/" + uiQuestions.questionView_ ;
					}
					questionId = strArr[1] ;
					question = faqService_.getQuestionById(questionId, sessionProvider) ;
					String categoryId = question.getCategoryId();
					FAQSetting faqSetting = uiQuestions.faqSetting_ ;
					String currentUser = FAQUtils.getCurrentUser() ;
					FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
					String[] moderator = new String[]{""};
					Boolean check = false ;
					Category category = new Category();
					if(categoryId != null && !categoryId.equals("null")){
						category = faqService_.getCategoryById(categoryId, sessionProvider) ;
						moderator = category.getModeratorsCategory() ;
					}
					if(faqSetting.getDisplayMode().equals("both")) {
						if(uiQuestions.faqSetting_.isAdmin() || Arrays.asList(moderator).contains(currentUser) || question.isActivated()) {
							check = true ;
						}
					} else {
						if(uiQuestions.faqSetting_.isAdmin() && question.isApproved() || Arrays.asList(moderator).contains(currentUser)&&question.isApproved()
								|| question.isActivated()&&question.isApproved()) {
							check = true ;
						}
					}
					if (check) {
						uiQuestions.pageList.setObjectRepare_(questionId);
						if(!categoryId.equals("null"))uiQuestions.setCategoryId(categoryId) ;
						else uiQuestions.setCategoryId(null) ;
						uiQuestions.viewAuthorInfor = category.isViewAuthorInfor();
						uiQuestions.setIsNotChangeLanguage() ;
						uiQuestions.listCateId_.clear() ;
						UIBreadcumbs breadcumbs = faqPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
						breadcumbs.setUpdataPath(null) ;
						String oldPath = "" ;
						if(categoryId != null && !categoryId.equals("null")){
							FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
							List<String> listPath = faqService.getCategoryPath(sessionProvider, categoryId) ;
							for(int i = listPath.size() -1 ; i >= 0; i --) {
								oldPath = oldPath + "/" + listPath.get(i);
							} 
						}
						newPath_ = "FAQService"+oldPath ;
						breadcumbs.setUpdataPath(newPath_);
						event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
						UIFAQContainer fAQContainer = uiQuestions.getAncestorOfType(UIFAQContainer.class) ;
					} else {
						uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-pending", null, ApplicationMessage.INFO)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
						sessionProvider.close();
						return ;
					}
				}

				List<String> listRelaId = new ArrayList<String>() ;
				for(String quesRelaId : question.getRelations()) {
					try {
						faqService_.getQuestionById(quesRelaId, sessionProvider) ;
						listRelaId.add(quesRelaId) ;
					} catch (Exception e) { }
				}
				if(listRelaId.size() < question.getRelations().length) {
					question.setRelations(listRelaId.toArray(new String[]{})) ;
					faqService_.saveQuestion(question, false, sessionProvider,uiQuestions.faqSetting_) ;
					for(int i = 0 ; i < uiQuestions.getListQuestion().size() ; i ++) {
						if(uiQuestions.getListQuestion().get(i).getId().equals(questionId)) {
							uiQuestions.getListQuestion().set(i, question) ;
							break ;
						}
					}
				}
				if( uiQuestions.questionView_ == null || uiQuestions.questionView_.trim().length() < 1 || !uiQuestions.questionView_.equals(questionId)){
					uiQuestions.questionView_ = questionId ; 
				} else {
					uiQuestions.isChangeLanguage = true ;
					uiQuestions.questionView_ = "" ;
				}
			} catch(javax.jcr.PathNotFoundException e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				uiQuestions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
				sessionProvider.close();
				return ;
			} catch (Exception e) { 
				e.printStackTrace() ;
			} finally {
				sessionProvider.close();
			}
			UIFAQContainer fAQContainer = uiQuestions.getAncestorOfType(UIFAQContainer.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
		}
	}

	static  public class ResponseQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			UIFAQPortlet portlet = question.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			Question question2 = null ;
			boolean nonCheck = false;
			String[] ids = event.getRequestContext().getRequestParameter(OBJECTID).split("/");
			String questionId = ids[0];
			if(ids.length > 1) nonCheck = true;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try{
				question2 = faqService_.getQuestionById(questionId, sessionProvider);
			} catch(javax.jcr.PathNotFoundException e) {
				UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				question.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(question) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				sessionProvider.close();
				return ;
			} catch (Exception e) { 
				e.printStackTrace() ;
			} finally {
				sessionProvider.close();
			}
			UIResponseForm responseForm = popupContainer.addChild(UIResponseForm.class, null, null) ;
			if(questionId.equals(question.questionView_)){
				responseForm.setQuestionId(question2, language_, nonCheck) ;
			} else {
				responseForm.setQuestionId(question2, "", nonCheck) ;
			}
			responseForm.setFAQSetting(question.faqSetting_);
			popupContainer.setId("FAQResponseQuestion") ;
			popupAction.activate(popupContainer, 720, 1000) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static  public class EditQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			questions.isChangeLanguage = false ;
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			Question question = null ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try{
				question = faqService_.getQuestionById(questionId, sessionProvider) ;
			} catch(javax.jcr.PathNotFoundException e) {
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				sessionProvider.close();
				return ;
			} catch (Exception e) { 
				e.printStackTrace() ;
			}  finally {
				sessionProvider.close();
			}
			UIQuestionForm questionForm = popupContainer.addChild(UIQuestionForm.class, null, null) ;
			questionForm.setQuestionId(question) ;
			questionForm.setFAQSetting(questions.faqSetting_);
			popupContainer.setId("EditQuestion") ;
			popupAction.activate(popupContainer, 600, 450) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static  public class DeleteQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			Question question = null ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try{
				question = faqService_.getQuestionById(questionId, sessionProvider) ;
			} catch (javax.jcr.PathNotFoundException e) {
				e.printStackTrace() ;
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				questions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				sessionProvider.close();
				return ;
			} catch (Exception e) { 
				e.printStackTrace() ;
			} finally {
				sessionProvider.close();
			}
			UIDeleteQuestion deleteQuestion = popupContainer.addChild(UIDeleteQuestion.class, null, null) ;
			deleteQuestion.setQuestionId(question) ;
			popupContainer.setId("FAQDeleteQuestion") ;
			popupAction.activate(popupContainer, 450, 250) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static  public class MoveQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try {
				faqService_.getQuestionById(questionId, sessionProvider) ;
			} catch (javax.jcr.PathNotFoundException e) {
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				questions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				sessionProvider.close();
				return ;
			} catch (Exception e) { 
				e.printStackTrace() ;
			} finally {
				sessionProvider.close();
			}
			UIMoveQuestionForm moveQuestionForm = popupContainer.addChild(UIMoveQuestionForm.class, null, null) ;
			moveQuestionForm.setQuestionId(questionId) ;
			popupContainer.setId("FAQMoveQuestion") ;
			moveQuestionForm.setFAQSetting(questions.faqSetting_) ;
			popupAction.activate(popupContainer, 600, 400) ;
			moveQuestionForm.setListCate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static  public class SendQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ; 
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = uiQuestions.getAncestorOfType(UIFAQPortlet.class) ;
			Question question = null ;
			String categoryId = null ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try{
				question = faqService_.getQuestionById(questionId, sessionProvider) ;
				categoryId = question.getCategoryId() ;
			} catch (Exception e) {
				UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				uiQuestions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				sessionProvider.close();
				return ;
			} finally {
				sessionProvider.close();
			}
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UISendMailForm sendMailForm = popupContainer.addChild(UISendMailForm.class, null, null) ;
			//link
			String link = uiQuestions.getLink().replaceFirst("UIQuestions", "UIBreadcumbs").replaceFirst("Setting", "ChangePath").replaceAll("&amp;", "&");
			String selectedNode = Util.getUIPortal().getSelectedNode().getUri() ;
			String portalName = "/" + Util.getUIPortal().getName() ;
			if(link.indexOf(portalName) > 0) {
				if(link.indexOf(portalName + "/" + selectedNode) < 0){
					link = link.replaceFirst(portalName, portalName + "/" + selectedNode) ;
				}									
			}	
			PortalRequestContext portalContext = Util.getPortalRequestContext();
			String url = portalContext.getRequest().getRequestURL().toString();
			url = url.replaceFirst("http://", "") ;
			url = url.substring(0, url.indexOf("/")) ;
			url = "http://" + url;
			String path = uiQuestions.getPathService(categoryId);
			if(!categoryId.equals("null")) path += "/"+ categoryId;
			link = link.replaceFirst("OBJECTID", path);
			link = url + link;
			sendMailForm.setLink(link);
			if(!questionId.equals(uiQuestions.questionView_) || FAQUtils.isFieldEmpty(language_)) sendMailForm.setUpdateQuestion(questionId , "") ;
			else sendMailForm.setUpdateQuestion(questionId , language_) ;
			popupContainer.setId("FAQSendMailForm") ;
			popupAction.activate(popupContainer, 700, 1000) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static  public class ChangeQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ; 
			String[] stringInput = event.getRequestContext().getRequestParameter(OBJECTID).split("/") ;
			int pos = Integer.parseInt(stringInput[0]) ;
			language_ = stringInput[1] ;
			for(QuestionLanguage questionLanguage : uiQuestions.listQuestionLanguage) {
				if(questionLanguage.getLanguage().equals(language_)) {
					uiQuestions.listQuestion_.get(pos).setQuestion(questionLanguage.getQuestion());
					uiQuestions.listQuestion_.get(pos).setLanguage(questionLanguage.getLanguage());
					uiQuestions.listQuestion_.get(pos).setResponses(questionLanguage.getResponse());
					uiQuestions.listQuestion_.get(pos).setResponseBy(questionLanguage.getResponseBy());
					uiQuestions.listQuestion_.get(pos).setDateResponse(questionLanguage.getDateResponse());
					break ;
				}
			}
			uiQuestions.isChangeLanguage = true ;
			UIFAQContainer fAQContainer = uiQuestions.getAncestorOfType(UIFAQContainer.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
		}
	}
}