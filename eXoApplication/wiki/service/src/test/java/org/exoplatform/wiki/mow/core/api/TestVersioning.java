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
package org.exoplatform.wiki.mow.core.api;

import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersion;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 7, 2010  
 */
public class TestVersioning extends AbstractMOWTestcase {
  
  public void testGetVersionHistory() throws Exception {
    PageImpl wikipage = createWikiPage(WikiType.PORTAL, "versioning", "testGetVersionHistory-001");
    assertNull(wikipage.getVersionableMixin());
    wikipage.makeVersionable();
    assertNotNull(wikipage.getVersionableMixin().getVersionHistory().getRootVersion().getCreated());
    assertTrue(wikipage.getVersionableMixin().isCheckedOut());
  }

  public void testCreateVersionHistoryTree() throws Exception {
    PageImpl wikipage = createWikiPage(WikiType.PORTAL, "versioning", "testCreateVersionHistoryTree-001");
    wikipage.makeVersionable();
    wikipage.getContent().setTitle("testCreateVersionHistoryTree");
    wikipage.getContent().setText("testCreateVersionHistoryTree-ver1.0");
    NTVersion ver1 = wikipage.checkin();
    assertNotNull(ver1);
    wikipage.checkout();
    wikipage.getContent().setText("testCreateVersionHistoryTree-ver2.0");
    NTVersion ver2 = wikipage.checkin();
    assertNotNull(ver2);
    assertNotSame(ver1, ver2);
    wikipage.checkout();
    wikipage.restore(ver1, false);
  }

}
