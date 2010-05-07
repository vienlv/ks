/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.wiki.webui.control.action;

import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.resolver.PageResolver;
import org.exoplatform.wiki.webui.PageMode;
import org.exoplatform.wiki.webui.UIWikiPageContentArea;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.control.filter.IsViewModeFilter;
import org.exoplatform.wiki.webui.control.listener.UIPageToolBarActionListener;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  events = {
    @EventConfig(listeners = EditPageActionComponent.EditPageActionListener.class)
  }
)
public class EditPageActionComponent extends UIComponent {

  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { new IsViewModeFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  
  public static class EditPageActionListener extends UIPageToolBarActionListener<EditPageActionComponent> {
    @Override
    protected void processEvent(Event<EditPageActionComponent> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIWikiPageContentArea pageContentArea = wikiPortlet.findFirstComponentOfType(UIWikiPageContentArea.class);
      UIFormTextAreaInput titleInput = new UIFormTextAreaInput(UIWikiPageContentArea.FIELD_TITLE,
                                                               UIWikiPageContentArea.FIELD_TITLE,
                                                               "Title");
      UIFormTextAreaInput markupInput = new UIFormTextAreaInput(UIWikiPageContentArea.FIELD_CONTENT,
                                                                UIWikiPageContentArea.FIELD_CONTENT,
                                                                "This is **bold**");
      String requestURL = Utils.getCurrentRequestURL();
      PageResolver pageResolver = (PageResolver) PortalContainer.getComponent(PageResolver.class);
      Page page = pageResolver.resolve(requestURL);
      titleInput.setValue(page.getContent().getTitle());
      titleInput.setEditable(false);
      markupInput.setValue(page.getContent().getText());
      pageContentArea.setPageMode(PageMode.EXISTED);
      pageContentArea.addUIFormInput(titleInput).setRendered(true);
      pageContentArea.addUIFormInput(markupInput).setRendered(true);
      
      wikiPortlet.setWikiMode(WikiMode.EDIT);
      super.processEvent(event);
    }
  }
  
}
