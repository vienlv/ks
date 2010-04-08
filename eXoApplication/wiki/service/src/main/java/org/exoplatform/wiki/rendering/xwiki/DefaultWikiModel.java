/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.wiki.rendering.xwiki;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiService;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.rendering.wiki.WikiModel;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Nov
 * 5, 2009
 */
@Component
public class DefaultWikiModel implements WikiModel {

  @Requirement
  private ComponentManager componentManager;
  
  @Requirement
  private Execution execution;
  
  private static final String DEFAULT_WIKI = "xwiki";
  
  private static final String DEFAULT_SPACE = "Main";
  
  private static final String DEFAULT_PAGE = "WebHome";
      
  private static final String DEFAULT_ATTACHMENT = "filename";
  
  public String getAttachmentURL(String documentName, String attachmentName) {
    return "#attachment";
  }

  public String getDocumentEditURL(String documentName, String anchor, String queryString) {
    return "#edit";
  }

  public String getDocumentViewURL(String documentName, String anchor, String queryString) {
    WikiContext wikiMarkupContext = getWikiMarkupContext(documentName);
    StringBuilder sb = new StringBuilder();
    sb.append(wikiMarkupContext.getPortalURI());
    sb.append("/");
    if("portal".equalsIgnoreCase(wikiMarkupContext.getType())){
      sb.append(wikiMarkupContext.getPortletURI());
      sb.append("/");
    }
    else{
      sb.append(wikiMarkupContext.getOwner());
      sb.append("/");
      sb.append(wikiMarkupContext.getPortletURI());
      sb.append("/");
    }
    sb.append(wikiMarkupContext.getPageId());
    return sb.toString();
  }

  public boolean isDocumentAvailable(String documentName) {
    // TODO : should look for pages in the model with the given title
    // (Page.findPageByTitle())
    Page page = null;
    try {
    WikiContext wikiMarkupContext = getWikiMarkupContext(documentName);
    WikiService wikiService = (WikiService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(WikiService.class);
    page = wikiService.getPageById(wikiMarkupContext.getType(),wikiMarkupContext.getOwner(),wikiMarkupContext.getPageId());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return (page != null);
  }

  private WikiContext getWikiMarkupContext(String documentName) {
    WikiContext wikiMarkupContext = new WikiContext();
    try {
      DocumentReferenceResolver<String> stringDocumentReferenceResolver = componentManager.lookup(DocumentReferenceResolver.class);
      EntityReference entityReference = stringDocumentReferenceResolver.resolve(documentName);

      ExecutionContext ec = execution.getContext();
      WikiContext wikiContext = null;
      if (ec != null) {
        wikiContext = (WikiContext) ec.getProperty("wikicontext");
      }

      wikiMarkupContext.setType(entityReference.extractReference(EntityType.WIKI).getName());
      wikiMarkupContext.setOwner(entityReference.extractReference(EntityType.SPACE).getName());
      wikiMarkupContext.setPageId(entityReference.extractReference(EntityType.DOCUMENT).getName());

      if (wikiContext != null) {

        wikiMarkupContext.setPortalURI(wikiContext.getPortalURI());
        wikiMarkupContext.setPortletURI(wikiContext.getPortletURI());

        if (DEFAULT_WIKI.equals(wikiMarkupContext.getType())) {
          wikiMarkupContext.setType(wikiContext.getType());
        }
        if (DEFAULT_SPACE.equals(wikiMarkupContext.getOwner())) {
          wikiMarkupContext.setOwner(wikiContext.getOwner());
        }
        if (DEFAULT_PAGE.equals(wikiMarkupContext.getPageId())) {
          wikiMarkupContext.setPageId(wikiContext.getPageId());
        }

      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return wikiMarkupContext;
  }
}
