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
package org.exoplatform.wiki.service;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 8, 2010  
 */
public class WikiContext extends WikiPageParams {
  
  public static final String WIKICONTEXT = "wikicontext";

  public static final String ACTION      = "action";

  public static final String ADDPAGE     = "AddPage";

  public static final String PAGETITLE   = "pageTitle";

  public static final String WIKITYPE    = "wikiType";

  public static final String WIKI        = "wiki";
  
  private String             pageTitle;
  
  private String             portalURI;

  private String             portletURI;
  
  private String             treeRestURI;
  
  private String             redirectURI;
  
  private String             pageTreeId;
  
  private String             restURI;

  public String getPageTitle() {
    return pageTitle;
  }

  public void setPageTitle(String pageTitle) {
    this.pageTitle = pageTitle;
  }
  
  public String getPortalURI() {
    return portalURI;
  }

  public void setPortalURI(String portalURI) {
    this.portalURI = portalURI;
  }

  public String getPortletURI() {
    return portletURI;
  }

  public void setPortletURI(String portletURI) {
    this.portletURI = portletURI;
  }

  public String getTreeRestURI() {
    return treeRestURI;
  }

  public void setTreeRestURI(String restURI) {
    this.treeRestURI = restURI;
  }

  public String getRedirectURI() {
    return redirectURI;
  }

  public void setRedirectURI(String redirectURI) {
    this.redirectURI = redirectURI;
  }

  public String getPageTreeId() {
    return pageTreeId;
  }

  public void setPageTreeId(String pageTreeId) {
    this.pageTreeId = pageTreeId;
  }

  /**
   * @return the restURI
   */
  public String getRestURI() {
    return restURI;
  }

  /**
   * @param restURI the restURI to set
   */
  public void setRestURI(String restURI) {
    this.restURI = restURI;
  }
  
}
