/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
 */
package org.exoplatform.wiki.webui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.organization.UIGroupMembershipSelector;
import org.exoplatform.webui.organization.account.UIGroupSelector;
import org.exoplatform.webui.organization.account.UIUserSelector;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.IDType;
import org.exoplatform.wiki.service.Permission;
import org.exoplatform.wiki.service.PermissionEntry;
import org.exoplatform.wiki.service.PermissionType;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.UIWikiPortlet.PopupLevel;
import org.exoplatform.wiki.webui.core.UIWikiForm;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Jan 5, 2011  
 */
@ComponentConfigs({
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPermissionForm.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiPermissionForm.AddEntryActionListener.class),
    @EventConfig(listeners = UIWikiPermissionForm.DeleteEntryActionListener.class),
    @EventConfig(listeners = UIWikiPermissionForm.OpenSelectUserFormActionListener.class),
    @EventConfig(listeners = UIWikiPermissionForm.SelectUserActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UIWikiPermissionForm.OpenSelectGroupFormActionListener.class),
    @EventConfig(listeners = UIWikiPermissionForm.SelectGroupActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UIWikiPermissionForm.OpenSelectMembershipFormActionListener.class),
    @EventConfig(listeners = UIWikiPermissionForm.SelectMembershipActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UIWikiPermissionForm.SaveActionListener.class),
    @EventConfig(listeners = UIWikiPermissionForm.CloseActionListener.class)
  }
),
@ComponentConfig(type = UIPopupWindow.class, id = "UIWikiUserPermissionPopupSelector", template = "system:/groovy/webui/core/UIPopupWindow.gtmpl", events = {
    @EventConfig(listeners = UIWikiPermissionForm.ClosePopupActionListener.class, name = "ClosePopup"),
    @EventConfig(listeners = UIWikiPermissionForm.SelectUserActionListener.class, name = "Add", phase = Phase.DECODE),
    @EventConfig(listeners = UIWikiPermissionForm.CloseUserPopupActionListener.class, name = "Close", phase = Phase.DECODE) })
})
public class UIWikiPermissionForm extends UIWikiForm implements UIPopupComponent {

  private List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>();

  private Scope scope;

  public static String ADD_ENTRY = "AddEntry";
  
  public static String DELETE_ENTRY = "DeleteEntry";
  
  public static String PERMISSION_POPUP_SELECTOR = "UIWikiPermissionPopupSelector";
  
  public static String USER_PERMISSION_POPUP_SELECTOR = "UIWikiUserPermissionPopupSelector";
  
  public static String OPEN_SELECT_USER_FORM = "OpenSelectUserForm";
  
  public static String OPEN_SELECT_GROUP_FORM= "OpenSelectGroupForm";
  
  public static String OPEN_SELECT_MEMBERSHIP_FORM= "OpenSelectMembershipForm";
  
  public static String GROUP_ICON = "GroupIcon";
  
  public static String USER_ICON = "UserIcon";
  
  public static String MEMBERSHIP_ICON = "MembershipIcon";
  
  public static String ADD_ICON = "Add";
  
  public static String SAVE = "Save";
  
  public static String CLOSE = "Close";

  public static enum Scope {
    WIKI, PAGE
  }

  public UIWikiPermissionForm() throws Exception {

    UIPermissionGrid permissionGrid = addChild(UIPermissionGrid.class, null, null);
    permissionGrid.setPermissionEntries(this.permissionEntries);

    UIFormInputWithActions owner = new UIFormInputWithActions("UIWikiPermissionOwner");
    owner.addUIFormInput(new UIFormStringInput("owner", "owner", null));
    List<ActionData> actions = new ArrayList<ActionData>();
    ActionData selectUser = new ActionData();
    selectUser.setActionListener("OPEN_SELECT_USER_FORM");
    selectUser.setActionType(ActionData.TYPE_ICON);
    selectUser.setActionName("OPEN_SELECT_USER_FORM");
    selectUser.setCssIconClass("USER_ICON");
    actions.add(selectUser);
    ActionData selectGroup = new ActionData();
    selectGroup.setActionListener("OPEN_SELECT_GROUP_FORM");
    selectGroup.setActionType(ActionData.TYPE_ICON);
    selectGroup.setActionName("OPEN_SELECT_GROUP_FORM");
    selectGroup.setCssIconClass(GROUP_ICON);
    actions.add(selectGroup);
    ActionData selectMembership = new ActionData();
    selectMembership.setActionListener("OPEN_SELECT_MEMBERSHIP_FORM");
    selectMembership.setActionType(ActionData.TYPE_ICON);
    selectMembership.setActionName("OPEN_SELECT_MEMBERSHIP_FORM");
    selectMembership.setCssIconClass(MEMBERSHIP_ICON);
    actions.add(selectMembership);
    ActionData addOwner = new ActionData();
    addOwner.setActionListener(ADD_ENTRY);
    addOwner.setActionType(ActionData.TYPE_ICON);
    addOwner.setActionName(ADD_ENTRY);
    addOwner.setCssIconClass(ADD_ICON);
    actions.add(addOwner);
    owner.setActionField("owner", actions);

    addChild(owner);

    addChild(UIPopupWindow.class, USER_PERMISSION_POPUP_SELECTOR, USER_PERMISSION_POPUP_SELECTOR);
    addChild(UIPopupWindow.class, null, PERMISSION_POPUP_SELECTOR);

    setActions(new String[] { SAVE, CLOSE });
  }

  public Scope getScope() {
    return scope;
  }

  public void setScope(Scope scope) {
    this.scope = scope;
    if (Scope.WIKI.equals(this.scope)) {
      this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.VIEW, WikiMode.EDITPAGE,
          WikiMode.ADDPAGE, WikiMode.DELETECONFIRM, WikiMode.VIEWREVISION, WikiMode.SHOWHISTORY,
          WikiMode.ADVANCEDSEARCH });
    } else if (Scope.PAGE.equals(this.scope)) {
      this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.VIEW });
    }
  }

  public void setPermission(List<PermissionEntry> permissionEntries) throws Exception {
    this.permissionEntries = permissionEntries;
    UIPermissionGrid permissionGrid = getChild(UIPermissionGrid.class);
    permissionGrid.setPermissionEntries(this.permissionEntries);
  }

  @Override
  public void activate() throws Exception {
  }

  @Override
  public void deActivate() throws Exception {
  }

  private void processPostAction() throws Exception {
    UIPermissionGrid permissionGrid = getChild(UIPermissionGrid.class);
    List<UIComponent> permissionEntries = permissionGrid.getChildren();
    List<PermissionEntry> permEntries = new ArrayList<PermissionEntry>();
    for (UIComponent uiPermissionEntry : permissionEntries) {
      PermissionEntry permissionEntry = ((UIWikiPermissionEntry) uiPermissionEntry).getPermissionEntry();
      Permission[] permissions = permissionEntry.getPermissions();
      for (Permission permission : permissions) {
        UIFormCheckBoxInput<Boolean> checkboxInput = ((UIWikiPermissionEntry) uiPermissionEntry).getChildById(permission.getPermissionType().toString()
            + permissionEntry.getId());
        permission.setAllowed(checkboxInput.getValue());
      }
      permEntries.add(permissionEntry);
    }
    setPermission(permEntries);
  }

  private HashMap<String, String[]> convertToPermissionMap(List<PermissionEntry> permissionEntries) {
    HashMap<String, String[]> permissionMap = new HashMap<String, String[]>();
    for (PermissionEntry permissionEntry : permissionEntries) {
      Permission[] permissions = permissionEntry.getPermissions();
      List<String> permlist = new ArrayList<String>();
      for (int i = 0; i < permissions.length; i++) {
        Permission permission = permissions[i];
        if (permission.isAllowed()) {
          if (permission.getPermissionType().equals(PermissionType.VIEWPAGE)) {
            permlist.add(org.exoplatform.services.jcr.access.PermissionType.READ);
          } else if (permission.getPermissionType().equals(PermissionType.EDITPAGE)) {
            permlist.add(org.exoplatform.services.jcr.access.PermissionType.ADD_NODE);
            permlist.add(org.exoplatform.services.jcr.access.PermissionType.REMOVE);
            permlist.add(org.exoplatform.services.jcr.access.PermissionType.SET_PROPERTY);
          }
        }
      }
      if (permlist.size() > 0) {
        permissionMap.put(permissionEntry.getId(), permlist.toArray(new String[permlist.size()]));
      }
    }
    return permissionMap;
  }

  static public class AddEntryActionListener extends EventListener<UIWikiPermissionForm> {
    @Override
    public void execute(Event<UIWikiPermissionForm> event) throws Exception {
      UIWikiPermissionForm uiWikiPermissionForm = event.getSource();
      Scope scope = uiWikiPermissionForm.getScope();
      PermissionEntry permissionEntry = new PermissionEntry();
      Permission[] permissions = null;
      if (Scope.WIKI.equals(scope)) {
        permissions = new Permission[4];
        permissions[0] = new Permission();
        permissions[0].setPermissionType(PermissionType.VIEWPAGE);
        permissions[1] = new Permission();
        permissions[1].setPermissionType(PermissionType.EDITPAGE);
        permissions[2] = new Permission();
        permissions[2].setPermissionType(PermissionType.ADMINPAGE);
        permissions[3] = new Permission();
        permissions[3].setPermissionType(PermissionType.ADMINSPACE);
      } else if (Scope.PAGE.equals(scope)) {
        permissions = new Permission[2];
        permissions[0] = new Permission();
        permissions[0].setPermissionType(PermissionType.VIEWPAGE);
        permissions[1] = new Permission();
        permissions[1].setPermissionType(PermissionType.EDITPAGE);
      }
      permissionEntry.setPermissions(permissions);
      UIFormInputWithActions inputWithActions = uiWikiPermissionForm.getChild(UIFormInputWithActions.class);
      UIFormStringInput uiFormStringInput = inputWithActions.getChild(UIFormStringInput.class);
      permissionEntry.setId(uiFormStringInput.getValue());
      permissionEntry.setIdType(IDType.USER);
      uiWikiPermissionForm.processPostAction();
      uiWikiPermissionForm.permissionEntries.add(permissionEntry);
      uiWikiPermissionForm.setPermission(uiWikiPermissionForm.permissionEntries);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWikiPermissionForm);
    }
  }

  static public class DeleteEntryActionListener extends EventListener<UIWikiPermissionForm> {
    @Override
    public void execute(Event<UIWikiPermissionForm> event) throws Exception {
      UIWikiPermissionForm uiWikiPermissionForm = event.getSource();
      String ownerId = event.getRequestContext().getRequestParameter(OBJECTID);
      for (PermissionEntry permissionEntry : uiWikiPermissionForm.permissionEntries) {
        if (permissionEntry.getId().equals(ownerId)) {
          uiWikiPermissionForm.permissionEntries.remove(permissionEntry);
          break;
        }
      }
      uiWikiPermissionForm.setPermission(uiWikiPermissionForm.permissionEntries);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWikiPermissionForm);
    }
  }

  static public class OpenSelectUserFormActionListener extends EventListener<UIWikiPermissionForm> {
    @Override
    public void execute(Event<UIWikiPermissionForm> event) throws Exception {
      UIWikiPermissionForm uiWikiPermissionForm = event.getSource();
      UIPopupWindow uiPopup = uiWikiPermissionForm.getChildById(USER_PERMISSION_POPUP_SELECTOR);
      uiPopup.setWindowSize(540, 0);
      UIUserSelector uiUserSelector = uiWikiPermissionForm.createUIComponent(UIUserSelector.class, null, null);
      uiUserSelector.setShowSearch(true);
      uiUserSelector.setShowSearchUser(true);
      uiUserSelector.setShowSearchGroup(false);
      uiPopup.setUIComponent(uiUserSelector);
      uiPopup.setShow(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWikiPermissionForm.getAncestorOfType(UIPopupContainer.class));
    }
  }

  static public class SelectUserActionListener extends EventListener<UIUserSelector> {
    @Override
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUserSelector = event.getSource();
      UIPopupWindow uiPopup = uiUserSelector.getParent();
      UIWikiPermissionForm uiWikiPermissionForm = uiPopup.getParent();
      String values = uiUserSelector.getSelectedUsers();
      UIFormInputWithActions inputWithActions = uiWikiPermissionForm.getChild(UIFormInputWithActions.class);
      UIFormStringInput uiFormStringInput = inputWithActions.getChild(UIFormStringInput.class);
      uiFormStringInput.setValue(values);
      uiPopup.setUIComponent(null);
      uiPopup.setShow(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWikiPermissionForm);
    }
  }

  static public class OpenSelectGroupFormActionListener extends EventListener<UIWikiPermissionForm> {
    @Override
    public void execute(Event<UIWikiPermissionForm> event) throws Exception {
      UIWikiPermissionForm uiWikiPermissionForm = event.getSource();
      UIPopupWindow uiPopup = uiWikiPermissionForm.getChildById(PERMISSION_POPUP_SELECTOR);
      uiPopup.setWindowSize(540, 0);
      UIGroupSelector uiGroupSelector = uiWikiPermissionForm.createUIComponent(UIGroupSelector.class, null, null);
      uiPopup.setUIComponent(uiGroupSelector);
      uiPopup.setShow(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWikiPermissionForm);
    }
  }

  static public class SelectGroupActionListener extends EventListener<UIGroupSelector> {
    @Override
    public void execute(Event<UIGroupSelector> event) throws Exception {
      UIWikiPermissionForm uiWikiPermissionForm = event.getSource().getParent().getParent();
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIFormInputWithActions inputWithActions = uiWikiPermissionForm.getChild(UIFormInputWithActions.class);
      UIFormStringInput uiFormStringInput = inputWithActions.getChild(UIFormStringInput.class);
      uiFormStringInput.setValue(groupId);
      UIPopupWindow uiPopup = uiWikiPermissionForm.getChildById(PERMISSION_POPUP_SELECTOR);
      uiPopup.setUIComponent(null);
      uiPopup.setShow(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWikiPermissionForm);
    }
  }

  static public class OpenSelectMembershipFormActionListener extends EventListener<UIWikiPermissionForm> {
    @Override
    public void execute(Event<UIWikiPermissionForm> event) throws Exception {
      UIWikiPermissionForm uiWikiPermissionForm = event.getSource();
      UIPopupWindow uiPopup = uiWikiPermissionForm.getChildById(PERMISSION_POPUP_SELECTOR);
      uiPopup.setWindowSize(540, 0);
      UIGroupMembershipSelector uiGroupMembershipSelector = uiWikiPermissionForm.createUIComponent(UIGroupMembershipSelector.class, null, null);
      uiPopup.setUIComponent(uiGroupMembershipSelector);
      uiPopup.setShow(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWikiPermissionForm);
    }
  }

  static public class SelectMembershipActionListener extends EventListener<UIGroupMembershipSelector> {
    @Override
    public void execute(Event<UIGroupMembershipSelector> event) throws Exception {
      UIWikiPermissionForm uiWikiPermissionForm = event.getSource().getParent().getParent();
      String membershipId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIFormInputWithActions inputWithActions = uiWikiPermissionForm.getChild(UIFormInputWithActions.class);
      UIFormStringInput uiFormStringInput = inputWithActions.getChild(UIFormStringInput.class);
      uiFormStringInput.setValue(membershipId);
      UIPopupWindow uiPopup = uiWikiPermissionForm.getChildById(PERMISSION_POPUP_SELECTOR);
      uiPopup.setUIComponent(null);
      uiPopup.setShow(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWikiPermissionForm);
    }
  }

  static public class ClosePopupActionListener extends EventListener<UIPopupWindow> {
    @Override
    public void execute(Event<UIPopupWindow> event) throws Exception {
      UIPopupWindow uiPopupWindow = event.getSource();
      // To avoid duplicate component id
      uiPopupWindow.setUIComponent(null);
      uiPopupWindow.setShow(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupWindow.getParent());
    }
  }

  static public class CloseUserPopupActionListener extends EventListener<UIUserSelector> {
    @Override
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIPopupWindow uiPopupWindow = event.getSource().getParent();
      // To avoid duplicate component id
      uiPopupWindow.setUIComponent(null);
      uiPopupWindow.setShow(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupWindow.getParent());
    }
  }

  static public class SaveActionListener extends EventListener<UIWikiPermissionForm> {
    @Override
    public void execute(Event<UIWikiPermissionForm> event) throws Exception {
      UIWikiPermissionForm uiWikiPermissionForm = event.getSource();
      Scope scope = uiWikiPermissionForm.getScope();
      UIWikiPortlet wikiPortlet = uiWikiPermissionForm.getAncestorOfType(UIWikiPortlet.class);
      uiWikiPermissionForm.processPostAction();
      if (Scope.WIKI.equals(scope)) {
        WikiService wikiService = uiWikiPermissionForm.getApplicationComponent(WikiService.class);
        WikiPageParams pageParams = Utils.getCurrentWikiPageParams();
        wikiService.setWikiPermission(pageParams.getType(), pageParams.getOwner(), uiWikiPermissionForm.permissionEntries);
      } else if (Scope.PAGE.equals(scope)) {
        PageImpl page = (PageImpl) Utils.getCurrentWikiPage();
        HashMap<String, String[]> permissionMap = uiWikiPermissionForm.convertToPermissionMap(uiWikiPermissionForm.permissionEntries);
        // Include ACL for administrators
        permissionMap.putAll(org.exoplatform.wiki.utils.Utils.getACLForAdmins());
        page.setPagePermission(permissionMap);
        page.setOverridePermission(true);
      }
      UIPopupContainer popupContainer = wikiPortlet.getPopupContainer(PopupLevel.L1);
      popupContainer.cancelPopupAction();
    }
  }

  static public class CloseActionListener extends EventListener<UIWikiPermissionForm> {
    @Override
    public void execute(Event<UIWikiPermissionForm> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIPopupContainer popupContainer = wikiPortlet.getPopupContainer(PopupLevel.L1);
      popupContainer.cancelPopupAction();
    }
  }

}