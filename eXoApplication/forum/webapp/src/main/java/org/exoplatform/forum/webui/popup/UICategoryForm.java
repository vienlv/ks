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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumTransformHTML;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.validator.MandatoryValidator;
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
			@EventConfig(listeners = UICategoryForm.SaveActionListener.class), 
			@EventConfig(listeners = UICategoryForm.AddPrivateActionListener.class, phase=Phase.DECODE),
			@EventConfig(listeners = UICategoryForm.CancelActionListener.class, phase=Phase.DECODE)
		}
)
public class UICategoryForm extends UIForm implements UIPopupComponent, UISelector{
	private String categoryId = "";
	public static final String CATEGORY_FORM = "category" ;

	public static final String FIELD_CATEGORYTITLE_INPUT = "CategoryTitle" ;
	public static final String FIELD_CATEGORYORDER_INPUT = "CategoryOrder" ;
	public static final String FIELD_DESCRIPTION_INPUT = "Description" ;
	public static final String FIELD_MODERAROR_MULTIVALUE = "moderators" ;
	public static final String FIELD_USERPRIVATE_MULTIVALUE = "UserPrivate" ;
	
	public UICategoryForm() throws Exception {
		UIFormStringInput categoryTitle = new UIFormStringInput(FIELD_CATEGORYTITLE_INPUT, FIELD_CATEGORYTITLE_INPUT, null);
		categoryTitle.addValidator(MandatoryValidator.class);
		UIFormStringInput categoryOrder = new UIFormStringInput(FIELD_CATEGORYORDER_INPUT, FIELD_CATEGORYORDER_INPUT, "0");
		categoryOrder.addValidator(PositiveNumberFormatValidator.class);
		UIFormStringInput description = new UIFormTextAreaInput(FIELD_DESCRIPTION_INPUT, FIELD_DESCRIPTION_INPUT, null);

		UIFormTextAreaInput userPrivate = new UIFormTextAreaInput(FIELD_USERPRIVATE_MULTIVALUE, FIELD_USERPRIVATE_MULTIVALUE, null);
		UIFormTextAreaInput moderators = new UIFormTextAreaInput(FIELD_MODERAROR_MULTIVALUE, FIELD_MODERAROR_MULTIVALUE, null);
		
		UIFormInputWithActions uicategory = new UIFormInputWithActions(CATEGORY_FORM);
		uicategory.addUIFormInput(categoryTitle);
		uicategory.addUIFormInput(categoryOrder);
		uicategory.addUIFormInput(userPrivate);
		uicategory.addUIFormInput(moderators);
		uicategory.addUIFormInput(description);

		String[]strings = new String[] {"SelectUser", "SelectMemberShip", "SelectGroup"}; 
		List<ActionData>actions = new ArrayList<ActionData>() ;
		
		ActionData ad ;
		int i = 0;
		for(String string : strings) {
			ad = new ActionData() ;
			ad.setActionListener("AddPrivate") ;
			ad.setActionParameter(String.valueOf(i)+"," + FIELD_USERPRIVATE_MULTIVALUE) ;
			ad.setCssIconClass(string + "Icon") ;
			ad.setActionName(string);
			actions.add(ad) ;
			++i;
		}
		uicategory.setActionField(FIELD_USERPRIVATE_MULTIVALUE, actions);
		actions = new ArrayList<ActionData>() ;
		i = 0;
		for(String string : strings) {
			ad = new ActionData() ;
			ad.setActionListener("AddPrivate") ;
			ad.setActionParameter(String.valueOf(i)+","+FIELD_MODERAROR_MULTIVALUE) ;
			ad.setCssIconClass(string + "Icon") ;
			ad.setActionName(string);
			actions.add(ad) ;
			++i;
		}
		uicategory.setActionField(FIELD_MODERAROR_MULTIVALUE, actions);
		
		addUIFormInput(uicategory) ;	
		this.setActions(new String[]{"Save","Cancel"}) ;
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	public void setCategoryValue(Category category, boolean isUpdate) throws Exception {
		if(isUpdate) {
			this.categoryId = category.getId() ;
			getUIStringInput(FIELD_CATEGORYTITLE_INPUT).setValue(ForumTransformHTML.unCodeHTML(category.getCategoryName())) ;
			getUIStringInput(FIELD_CATEGORYORDER_INPUT).setValue(Long.toString(category.getCategoryOrder())) ;
			getUIFormTextAreaInput(FIELD_DESCRIPTION_INPUT).setDefaultValue(ForumTransformHTML.unCodeHTML(category.getDescription())) ;
			String userPrivate = ForumUtils.unSplitForForum(category.getUserPrivate());
			getUIFormTextAreaInput(FIELD_USERPRIVATE_MULTIVALUE).setValue(userPrivate) ;
			String moderator = ForumUtils.unSplitForForum(category.getModerators());
			getUIFormTextAreaInput(FIELD_MODERAROR_MULTIVALUE).setValue(moderator) ;
		}
	}

	public void updateSelect(String selectField, String value) throws Exception {
		UIFormStringInput fieldInput = getUIStringInput(selectField) ;
		String values = fieldInput.getValue() ;
		if(!ForumUtils.isEmpty(values)) {
			values = ForumUtils.removeSpaceInString(values);
			if(!ForumUtils.isStringInStrings(values.split(","), value)){
				if(values.lastIndexOf(",") != (values.length() - 1)) values = values + ",";
				values = values + value ;
			} 
		} else values = value ;
		fieldInput.setValue(values) ;
	}
	
	static	public class SaveActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm uiForm = event.getSource() ;
			String categoryTitle = uiForm.getUIStringInput(FIELD_CATEGORYTITLE_INPUT).getValue();
			int maxText = ForumUtils.MAXTITLE ;
			if(categoryTitle.length() > maxText) {
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				Object[] args = { uiForm.getLabel(FIELD_CATEGORYTITLE_INPUT), String.valueOf(maxText) };
				uiApp.addMessage(new ApplicationMessage("NameValidator.msg.warning-long-text", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
			categoryTitle = ForumTransformHTML.enCodeHTML(categoryTitle);
			String description = uiForm.getUIFormTextAreaInput(FIELD_DESCRIPTION_INPUT).getValue();
			if(!ForumUtils.isEmpty(description) && description.length() > maxText) {
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				Object[] args = { uiForm.getLabel(FIELD_DESCRIPTION_INPUT), String.valueOf(maxText) };
				uiApp.addMessage(new ApplicationMessage("NameValidator.msg.warning-long-text", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
			description = ForumTransformHTML.enCodeHTML(description);
			String categoryOrder = uiForm.getUIStringInput(FIELD_CATEGORYORDER_INPUT).getValue();
			if(ForumUtils.isEmpty(categoryOrder)) categoryOrder = "0";
			categoryOrder = ForumUtils.removeZeroFirstNumber(categoryOrder) ;
			if(categoryOrder.length() > 3) {
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				Object[] args = { uiForm.getLabel(FIELD_CATEGORYORDER_INPUT) };
				uiApp.addMessage(new ApplicationMessage("NameValidator.msg.erro-large-number", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
			String moderator = uiForm.getUIFormTextAreaInput(FIELD_MODERAROR_MULTIVALUE).getValue();
			moderator = ForumUtils.removeSpaceInString(moderator) ;
			moderator = ForumUtils.removeStringResemble(moderator) ;
			String []moderators = ForumUtils.splitForForum(moderator);
			if(!ForumUtils.isEmpty(moderator)) {
				String erroUser = ForumSessionUtils.checkValueUser(moderator) ;
				if(!ForumUtils.isEmpty(erroUser)) {
					UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
					Object[] args = { uiForm.getLabel(FIELD_USERPRIVATE_MULTIVALUE), erroUser };
					uiApp.addMessage(new ApplicationMessage("NameValidator.msg.erroUser-input", args, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					return ;
				}
			} else {moderators = new String[]{" "};}
		
			String userPrivate = uiForm.getUIFormTextAreaInput(FIELD_USERPRIVATE_MULTIVALUE).getValue();
			if(!ForumUtils.isEmpty(userPrivate) && !ForumUtils.isEmpty(moderator)) {
				userPrivate = userPrivate + "," + moderator;
			}
			userPrivate = ForumUtils.removeSpaceInString(userPrivate) ;
			userPrivate = ForumUtils.removeStringResemble(userPrivate) ;
			String []userPrivates = ForumUtils.splitForForum(userPrivate);
			if(!ForumUtils.isEmpty(userPrivate)) {
				String erroUser = ForumSessionUtils.checkValueUser(userPrivate) ;
				if(!ForumUtils.isEmpty(erroUser)) {
					UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
					Object[] args = { uiForm.getLabel(FIELD_USERPRIVATE_MULTIVALUE), erroUser };
					uiApp.addMessage(new ApplicationMessage("NameValidator.msg.erroUser-input", args, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					return ;
				}
			} else {userPrivates = new String[]{" "};}

			
			String userName = ForumSessionUtils.getCurrentUser();
			Category cat = new Category();
			cat.setOwner(userName) ;
			cat.setCategoryName(categoryTitle.trim()) ;
			cat.setCategoryOrder(Long.parseLong(categoryOrder)) ;
			cat.setCreatedDate(new Date()) ;
			cat.setDescription(description) ;
			cat.setModifiedBy(userName) ;
			cat.setModifiedDate(new Date()) ;
			cat.setUserPrivate(userPrivates) ;
			cat.setModerators(moderators) ;
			
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			ForumService forumService =	(ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			try {
				if(!ForumUtils.isEmpty(uiForm.categoryId)) {
					cat.setId(uiForm.categoryId) ;
					forumService.saveCategory(sProvider, cat, false);
				} else {
					forumService.saveCategory(sProvider, cat, true);
				}
			} catch (Exception e) {
				sProvider.close();
				throw e;
			}finally {
				sProvider.close();
			}
			forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
			forumPortlet.findFirstComponentOfType(UICategory.class).setIsEditForum(true) ;
			forumPortlet.cancelAction() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static	public class AddPrivateActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm categoryForm = event.getSource() ;
			String[] objects = ((String)event.getRequestContext().getRequestParameter(OBJECTID)).split(",");	;
			String type = objects[0];
			String param = objects[1];
			UIPopupContainer popupContainer = categoryForm.getAncestorOfType(UIPopupContainer.class) ;
			UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
			UIGroupSelector uiGroupSelector = popupAction.activate(UIGroupSelector.class, 600) ;
			if(type.equals("0")) uiGroupSelector.setId("UIUserSelector");
			else if(type.equals("1")) uiGroupSelector.setId("UIMemberShipSelector");
			uiGroupSelector.setType(type) ;
			uiGroupSelector.setSelectedGroups(null) ;
			uiGroupSelector.setComponent(categoryForm, new String[]{param}) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}

	static	public class CancelActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
}
