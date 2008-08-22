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
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumEventQuery;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.popup.UIForumInputWithActions;
import org.exoplatform.forum.webui.popup.UIGroupSelector;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UISelectComponent;
import org.exoplatform.forum.webui.popup.UISelector;
import org.exoplatform.forum.webui.popup.UIForumInputWithActions.ActionData;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.PositiveNumberFormatValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIFormForum.gtmpl",
		events = {
			@EventConfig(listeners = UISearchForm.SearchActionListener.class),	
			@EventConfig(listeners = UISearchForm.OnchangeActionListener.class, phase = Phase.DECODE),	
			@EventConfig(listeners = UISearchForm.ResetFieldActionListener.class, phase = Phase.DECODE),	
			@EventConfig(listeners = UISearchForm.AddValuesUserActionListener.class, phase = Phase.DECODE),	
			@EventConfig(listeners = UISearchForm.CancelActionListener.class, phase = Phase.DECODE)			
		}
)
public class UISearchForm extends UIForm implements UISelector {
	final static	private String FIELD_INPUTSEARCH_FORM = "InputSearchForm" ;
	
	final static	private String FIELD_SEARCHVALUE_INPUT = "SearchValue" ;
	final static	private String FIELD_SEARCHVALUEIN_SELECTBOX = "SearchValueIn" ;
	final static	private String FIELD_SEARCHUSER_INPUT = "SearchUser" ;
	final static	private String FIELD_SEARCHTYPE_SELECTBOX = "SearchType" ;
	
	final static	private String FIELD_TOPICCOUNTMIN_INPUT = "TopicCountMin" ;
	final static	private String FIELD_TOPICCOUNTMAX_INPUT = "TopicCountMax" ;
	final static	private String FIELD_POSTCOUNTMIN_INPUT = "PostCountMin" ;
	final static	private String FIELD_POSTCOUNTMAX_INPUT = "PostCountMax" ;
	final static	private String FIELD_VIEWCOUNTMAX_INPUT = "ViewCountMin" ;
	final static	private String FIELD_VIEWCOUNTMIN_INPUT = "ViewCountMax" ;
	final static	private String FIELD_ISLOCK_SELECTBOX = "IsLock" ;
	final static	private String FIELD_ISCLOSED_SELECTBOX = "IsClosed" ;
	final static	private String FIELD_MODERATOR_INPUT = "Moderator" ;
	
	final static	private String FROMDATECREATED = "FromDateCreated" ;
	final static	private String TODATECREATED = "ToDateCreated" ;
	
	final static	private String FROMDATECREATEDLASTPOST = "FromDateCreatedLastPost" ;
	final static	private String TODATECREATEDLASTPOST = "ToDateCreatedLastPost" ;
	
	private UserProfile userProfile = null;
	
	public void setUserProfile(UserProfile userProfile) throws Exception {
		try {
			this.userProfile = userProfile ;
    } catch (Exception e) {
    	this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
    }
  }
	private boolean getIsAdmin() {
		if(this.userProfile != null) {
			if(this.userProfile.getUserRole() == 0) return true ;
		}
		return false ;
	}
	public UISearchForm() throws Exception {
		UIFormStringInput searchValue = new UIFormStringInput(FIELD_SEARCHVALUE_INPUT, FIELD_SEARCHVALUE_INPUT, null) ;
		UIFormStringInput searchUser = new UIFormStringInput(FIELD_SEARCHUSER_INPUT, FIELD_SEARCHUSER_INPUT, null) ;
		List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>(ForumUtils.CATEGORY, Utils.CATEGORY)) ;
		list.add(new SelectItemOption<String>(ForumUtils.FORUM, Utils.FORUM)) ;
		list.add(new SelectItemOption<String>(ForumUtils.THREAD, Utils.TOPIC)) ;
		list.add(new SelectItemOption<String>(ForumUtils.POST, Utils.POST)) ;
		UIFormSelectBox searchType = new UIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX, FIELD_SEARCHTYPE_SELECTBOX, list) ;
		searchType.setOnChange("Onchange") ;
		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("Search Entire", "entire")) ;
		list.add(new SelectItemOption<String>("Search Titles Only", "title")) ;
		UIFormSelectBox textIn = new UIFormSelectBox(FIELD_SEARCHVALUEIN_SELECTBOX, FIELD_SEARCHVALUEIN_SELECTBOX, list) ;
		textIn.setValue("entire");
		
		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("All", "all")) ;
		list.add(new SelectItemOption<String>("UnLock", "false")) ;
		list.add(new SelectItemOption<String>("Locked", "true")) ;
		UIFormSelectBox isLock = new UIFormSelectBox(FIELD_ISLOCK_SELECTBOX, FIELD_ISLOCK_SELECTBOX, list) ;
		isLock.setValue("all");
		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("All", "all")) ;
		list.add(new SelectItemOption<String>("Open", "false")) ;
		list.add(new SelectItemOption<String>("Closed", "true")) ;
		UIFormSelectBox isClosed = new UIFormSelectBox(FIELD_ISCLOSED_SELECTBOX, FIELD_ISCLOSED_SELECTBOX, list) ;
		isLock.setValue("all");
		
		UIFormDateTimeInput FromDateCreated = new UIFormDateTimeInput(FROMDATECREATED, FROMDATECREATED, null, false) ;
		UIFormDateTimeInput ToDateCreated = new UIFormDateTimeInput(TODATECREATED, TODATECREATED, null, false) ;
		UIFormDateTimeInput FromDateCreatedLastPost = new UIFormDateTimeInput(FROMDATECREATEDLASTPOST, FROMDATECREATEDLASTPOST, null, false) ;
		UIFormDateTimeInput ToDateCreatedLastPost = new UIFormDateTimeInput(TODATECREATEDLASTPOST, TODATECREATEDLASTPOST, null, false) ;

		UIFormStringInput topicCountMin = new UIFormStringInput(FIELD_TOPICCOUNTMIN_INPUT, FIELD_TOPICCOUNTMIN_INPUT, null) ;
		topicCountMin.addValidator(PositiveNumberFormatValidator.class) ;
		UIFormStringInput topicCountMax = new UIFormStringInput(FIELD_TOPICCOUNTMAX_INPUT, FIELD_TOPICCOUNTMAX_INPUT, null) ;
		topicCountMax.addValidator(PositiveNumberFormatValidator.class) ;

		UIFormStringInput postCountMin = new UIFormStringInput(FIELD_POSTCOUNTMIN_INPUT, FIELD_POSTCOUNTMIN_INPUT, null) ;
		postCountMin.addValidator(PositiveNumberFormatValidator.class) ;
		UIFormStringInput postCountMax = new UIFormStringInput(FIELD_POSTCOUNTMAX_INPUT, FIELD_POSTCOUNTMAX_INPUT, null) ;
		postCountMin.addValidator(PositiveNumberFormatValidator.class) ;
		
		UIFormStringInput viewCountMin = new UIFormStringInput(FIELD_VIEWCOUNTMIN_INPUT, FIELD_VIEWCOUNTMIN_INPUT, null) ;
		viewCountMin.addValidator(PositiveNumberFormatValidator.class) ;viewCountMin.setRendered(false) ;
		UIFormStringInput viewCountMax = new UIFormStringInput(FIELD_VIEWCOUNTMAX_INPUT, FIELD_VIEWCOUNTMAX_INPUT, null) ;
		viewCountMax.addValidator(PositiveNumberFormatValidator.class) ;viewCountMax.setRendered(false) ;
		
		UIFormStringInput moderator = new UIFormStringInput(FIELD_MODERATOR_INPUT, FIELD_MODERATOR_INPUT, null) ;
		
		UIForumInputWithActions inputSearchForm = new UIForumInputWithActions(FIELD_INPUTSEARCH_FORM);
		inputSearchForm.addUIFormInput(searchType) ;
		inputSearchForm.addUIFormInput(searchValue) ;
		inputSearchForm.addUIFormInput(textIn) ;
		inputSearchForm.addUIFormInput(searchUser) ;
		inputSearchForm.addUIFormInput(isLock) ;
		inputSearchForm.addUIFormInput(isClosed) ;
		inputSearchForm.addUIFormInput(FromDateCreated) ;
		inputSearchForm.addUIFormInput(ToDateCreated) ;
		
		inputSearchForm.addUIFormInput(topicCountMin) ;
		inputSearchForm.addUIFormInput(topicCountMax) ;
		inputSearchForm.addUIFormInput(postCountMin) ;
		inputSearchForm.addUIFormInput(postCountMax) ;
		inputSearchForm.addUIFormInput(FromDateCreatedLastPost) ;
		inputSearchForm.addUIFormInput(ToDateCreatedLastPost) ;
		inputSearchForm.addUIFormInput(viewCountMin) ;
		inputSearchForm.addUIFormInput(viewCountMax) ;
		inputSearchForm.addUIFormInput(moderator) ;
		
		addUIFormInput(inputSearchForm) ;
		List<ActionData> actions = new ArrayList<ActionData>() ;;
		ActionData ad = new ActionData() ;
		ad.setActionListener("AddValuesUser") ;
		ad.setCssIconClass("SelectUserIcon") ;
		ad.setActionName(FIELD_SEARCHUSER_INPUT);
		actions.add(ad) ;
		inputSearchForm.setActionField(FIELD_SEARCHUSER_INPUT, actions);
	}
	
	public void setSelectType(String type) {
	  this.getUIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX).setValue(type) ;
  }
	
	public void setValueOnchange(boolean isLastDate, boolean islock, boolean isClose, boolean isTopicCount, boolean isPostCount, boolean isViewCount, boolean isModerator){
		UIForumInputWithActions inputSearchForm = this.getChildById(FIELD_INPUTSEARCH_FORM);
		UIFormDateTimeInput fromDateCreatedLastPost = inputSearchForm.getUIFormDateTimeInput(FROMDATECREATEDLASTPOST).setRendered(isLastDate) ;
		UIFormDateTimeInput toDateCreatedLastPost   = inputSearchForm.getUIFormDateTimeInput(TODATECREATEDLASTPOST).setRendered(isLastDate) ;
		UIFormSelectBox isLock   = inputSearchForm.getUIFormSelectBox(FIELD_ISLOCK_SELECTBOX).setRendered(islock);
		if(isClose) {
			isClose = getIsAdmin();
		}
		UIFormSelectBox isClosed = inputSearchForm.getUIFormSelectBox(FIELD_ISCLOSED_SELECTBOX).setRendered(isClose);
		UIFormStringInput topicCountMin = inputSearchForm.getUIStringInput(FIELD_TOPICCOUNTMIN_INPUT).setRendered(isTopicCount);
		UIFormStringInput topicCountMax = inputSearchForm.getUIStringInput(FIELD_TOPICCOUNTMAX_INPUT).setRendered(isTopicCount);
		UIFormStringInput postCountMax  = inputSearchForm.getUIStringInput(FIELD_POSTCOUNTMAX_INPUT).setRendered(isPostCount);
		UIFormStringInput postCountMin  = inputSearchForm.getUIStringInput(FIELD_POSTCOUNTMIN_INPUT).setRendered(isPostCount);
		UIFormStringInput viewCountMax  = inputSearchForm.getUIStringInput(FIELD_VIEWCOUNTMAX_INPUT).setRendered(isViewCount);
		UIFormStringInput viewCountMin  = inputSearchForm.getUIStringInput(FIELD_VIEWCOUNTMIN_INPUT).setRendered(isViewCount);
		UIFormStringInput moderator     = inputSearchForm.getUIStringInput(FIELD_MODERATOR_INPUT).setRendered(isModerator);
		inputSearchForm.getUIStringInput(FIELD_SEARCHVALUE_INPUT).setValue("") ;
		inputSearchForm.getUIFormSelectBox(FIELD_SEARCHVALUEIN_SELECTBOX).setValue("entire");
		inputSearchForm.getUIFormDateTimeInput(FROMDATECREATED).setValue("") ;
		inputSearchForm.getUIFormDateTimeInput(TODATECREATED).setValue("") ;
		inputSearchForm.getUIStringInput(FIELD_SEARCHUSER_INPUT).setValue("") ;
		fromDateCreatedLastPost.setValue("") ;
		toDateCreatedLastPost.setValue("") ;
		isLock.setValue("all") ;
		isClosed.setValue("all") ;
		topicCountMax.setValue("") ;
		topicCountMin.setValue("") ;
		postCountMax.setValue("") ;
		postCountMin.setValue("") ;
		viewCountMax.setValue("") ;
		viewCountMin.setValue("") ;
		moderator.setValue("") ;
	}
	
	public String getLabel(ResourceBundle res, String id) throws Exception {
    String label = getId() + ".label." + id;    
    try {
    	return res.getString(label);
    } catch (Exception e) {
			return id ;
		}
  }
  
	private String checkValue(String input) throws Exception {
		if(!ForumUtils.isEmpty(input)){
			try {
				Integer.parseInt(input.trim()) ;
				return input.trim() ;
	    } catch (NumberFormatException e) {
		    return null;
	    }
		} else return null;
	}
	
  public String[] getActions() {
    return new String[]{"Search","Onchange", "Cancel"} ;
  }
  
  public void updateSelect(String selectField, String value) throws Exception {
    UIFormStringInput fieldInput = getUIStringInput(selectField) ;
    String values = fieldInput.getValue() ;
    if(!ForumUtils.isEmpty(values)) {
      if(!ForumUtils.isStringInStrings(values.split(","), value)){
        if(values.trim().lastIndexOf(",") != (values.length() - 1)) values = values.trim() + ",";
        values = values + value ;
      }
    } else values = value ;
    fieldInput.setValue(values) ;
  }
  
  private Calendar getCalendar(UIFormDateTimeInput dateTimeInput, String faled) throws Exception{
  	Calendar calendar = dateTimeInput.getCalendar();
  	if(!ForumUtils.isEmpty(dateTimeInput.getValue())){
  		if(calendar == null){
  			Object[] args = {faled};
				throw new MessageException(new ApplicationMessage("NameValidator.msg.erro-format-date", args, ApplicationMessage.WARNING)) ;
  		}
  	}
  	return calendar;
  }
  
	static	public class SearchActionListener extends EventListener<UISearchForm> {
    public void execute(Event<UISearchForm> event) throws Exception {
			UISearchForm uiForm = event.getSource() ;
			UIForumInputWithActions inputSearchForm = uiForm.getChildById(FIELD_INPUTSEARCH_FORM);
			String keyValue = inputSearchForm.getUIStringInput(FIELD_SEARCHVALUE_INPUT).getValue() ;
			String special = "\\,.?!`~/][)(;#@$%^&*<>-_+=";
			for (int i = 0; i < special.length(); i++) {
	      char c = special.charAt(i);
	      if(keyValue.indexOf(c) >= 0) {
	      	UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UIQuickSearchForm.msg.failure", null, ApplicationMessage.WARNING)) ;
					return ;
	      }
      }
			String type = inputSearchForm.getUIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX).getValue() ;
			String valueIn = inputSearchForm.getUIFormSelectBox(FIELD_SEARCHVALUEIN_SELECTBOX).getValue() ;
			String path = "" ;
			String byUser = inputSearchForm.getUIStringInput(FIELD_SEARCHUSER_INPUT).getValue() ;
			String isLock = inputSearchForm.getUIFormSelectBox(FIELD_ISLOCK_SELECTBOX).getValue();
			String isClosed = "all" ;
			String remain = "";
			if(uiForm.getIsAdmin()) {
				isClosed = inputSearchForm.getUIFormSelectBox(FIELD_ISCLOSED_SELECTBOX).getValue();
			} else {
				if(type.equals(Utils.FORUM)) {
					isClosed = "false";
				}else if(type.equals(Utils.TOPIC)) {
					isClosed = "false"; remain = "@exo:isActiveByForum='true'";
				}else if(type.equals(Utils.POST)) remain = "@exo:isActiveByTopic='true'";
			}
			String topicCountMin = inputSearchForm.getUIStringInput(FIELD_TOPICCOUNTMIN_INPUT).getValue();
			String topicCountMax = inputSearchForm.getUIStringInput(FIELD_TOPICCOUNTMAX_INPUT).getValue();
			String postCountMin = inputSearchForm.getUIStringInput(FIELD_POSTCOUNTMIN_INPUT).getValue();
			String postCountMax = inputSearchForm.getUIStringInput(FIELD_POSTCOUNTMAX_INPUT).getValue();
			String viewCountMin = inputSearchForm.getUIStringInput(FIELD_VIEWCOUNTMIN_INPUT).getValue();
			String viewCountMax = inputSearchForm.getUIStringInput(FIELD_VIEWCOUNTMAX_INPUT).getValue();
			String moderator = inputSearchForm.getUIStringInput(FIELD_MODERATOR_INPUT).getValue();
			Calendar fromDateCreated = uiForm.getCalendar(inputSearchForm.getUIFormDateTimeInput(FROMDATECREATED), FROMDATECREATED);
			Calendar toDateCreated= uiForm.getCalendar(inputSearchForm.getUIFormDateTimeInput(TODATECREATED), TODATECREATED);
			Calendar fromDateCreatedLastPost = uiForm.getCalendar(inputSearchForm.getUIFormDateTimeInput(FROMDATECREATEDLASTPOST), FROMDATECREATEDLASTPOST);
			Calendar toDateCreatedLastPost = uiForm.getCalendar(inputSearchForm.getUIFormDateTimeInput(TODATECREATEDLASTPOST), TODATECREATEDLASTPOST);
			ForumEventQuery eventQuery = new ForumEventQuery() ;
			eventQuery.setType(type) ;
			eventQuery.setKeyValue(keyValue) ;
			eventQuery.setValueIn(valueIn) ;
			eventQuery.setPath(path) ;
			eventQuery.setByUser(byUser);
			eventQuery.setIsLock(isLock) ;
			eventQuery.setIsClose(isClosed) ;
			eventQuery.setTopicCountMin(uiForm.checkValue(topicCountMin)) ;
			eventQuery.setTopicCountMax(uiForm.checkValue(topicCountMax)) ;
			eventQuery.setPostCountMin(uiForm.checkValue(postCountMin)) ;
			eventQuery.setPostCountMax(uiForm.checkValue(postCountMax)) ;
			eventQuery.setViewCountMin(uiForm.checkValue(viewCountMin)) ;
			eventQuery.setViewCountMax(uiForm.checkValue(viewCountMax)) ;
			eventQuery.setModerator(moderator) ;
			eventQuery.setFromDateCreated(fromDateCreated) ;
			eventQuery.setToDateCreated(toDateCreated) ;
			eventQuery.setFromDateCreatedLastPost(fromDateCreatedLastPost) ;
			eventQuery.setToDateCreatedLastPost(toDateCreatedLastPost) ;

			eventQuery.getPathQuery() ;
			boolean isEmpty = eventQuery.getIsAnd() ;
			eventQuery.setRemain(remain) ;
			if(!isEmpty) {
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("NameValidator.msg.erro-empty-search", null, ApplicationMessage.WARNING)) ;
				return ;
			}
			ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			List<ForumSearch> list = null ;
			try {
				list = forumService.getAdvancedSearch(ForumSessionUtils.getSystemProvider(),eventQuery);
			}catch (Exception e) {
				e.printStackTrace();
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIQuickSearchForm.msg.failure", null, ApplicationMessage.WARNING)) ;
				return ;
			}
			
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(ForumUtils.CATEGORIES) ;
			UICategories categories = forumPortlet.findFirstComponentOfType(UICategories.class);
			categories.setIsRenderChild(true) ;				
			UIForumListSearch listSearchEvent = categories.getChild(UIForumListSearch.class) ;
			listSearchEvent.setListSearchEvent(list) ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(ForumUtils.FIELD_EXOFORUM_LABEL) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}

	static	public class OnchangeActionListener extends EventListener<UISearchForm> {
		public void execute(Event<UISearchForm> event) throws Exception {
			UISearchForm uiForm = event.getSource() ;
			String type = uiForm.getUIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX).getValue() ;
			if(type.equals(Utils.FORUM)) {
				uiForm.setValueOnchange(false, true, true, true, true, false, true) ;
			} else if(type.equals(Utils.TOPIC)){
				uiForm.setValueOnchange(true, true, true, false, true, true, false) ;
			} else {
				uiForm.setValueOnchange(false, false, false, false, false, false, false) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
		}
	}
	
	static	public class ResetFieldActionListener extends EventListener<UISearchForm> {
		public void execute(Event<UISearchForm> event) throws Exception {
			UISearchForm uiForm = event.getSource() ;
			uiForm.getUIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX).setValue(Utils.CATEGORY);
			uiForm.setValueOnchange(false, false, false, false, false, false, false) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
		}
	}

	static	public class CancelActionListener extends EventListener<UISearchForm> {
		public void execute(Event<UISearchForm> event) throws Exception {
			UISearchForm uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = (UIForumPortlet)uiForm.getParent() ;
			forumPortlet.updateIsRendered(ForumUtils.CATEGORIES) ;
			UICategories categories = forumPortlet.findFirstComponentOfType(UICategories.class);
			categories.setIsRenderChild(false) ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}

	static	public class AddValuesUserActionListener extends EventListener<UISearchForm> {
    public void execute(Event<UISearchForm> event) throws Exception {
    	UISearchForm searchForm = event.getSource() ;
				UIForumPortlet forumPortlet = searchForm.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class).setRendered(true) ;
				UIGroupSelector uiGroupSelector = popupAction.activate(UIGroupSelector.class, 500) ;
				uiGroupSelector.setId("UIUserSelector");
	      uiGroupSelector.setType(UISelectComponent.TYPE_USER) ;
	      uiGroupSelector.setSelectedGroups(null) ;
	      uiGroupSelector.setComponent(searchForm, new String[]{FIELD_SEARCHUSER_INPUT}) ;
	      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}








}















