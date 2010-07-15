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
package org.exoplatform.wiki.webui;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.wiki.rendering.RenderingService;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPageContentArea.gtmpl"
)
public class UIWikiPageContentArea extends UIContainer {

  private String htmlOutput;
  private PageMode pageMode = PageMode.NEW;
  
  public UIWikiPageContentArea(){
  }

  public PageMode getPageMode() {
    return pageMode;
  }

  public void setPageMode(PageMode pageMode) {
    this.pageMode = pageMode;
  }

  public String getHtmlOutput() {
    return htmlOutput;
  }

  public void setHtmlOutput(String output) {
    this.htmlOutput = output;
  }
  
  public void renderWikiMarkup(String markup, String syntaxId) throws Exception {
    RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
    this.htmlOutput = renderingService.render(markup, syntaxId, Syntax.XHTML_1_0.toIdString());
  }
  
  public WikiMode getWikiMode(){
    return getAncestorOfType(UIWikiPortlet.class).getWikiMode();
  }
  
}