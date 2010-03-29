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
package org.exoplatform.wiki.mow.core.api.wiki;

import java.util.Collection;
import java.util.Map;

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.OneToMany;
import org.exoplatform.wiki.mow.api.Wiki;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public abstract class WikiContainer<T extends Wiki> {

  @OneToMany
  public abstract Map<String, T> getWikis();

  /*
   * @OneToOne public abstract WikiStoreImpl getMultiWiki();
   */

  public T addWiki(String wikiName) {
    T wiki = createWiki();
    getWikis().put(wikiName, wiki);
    return wiki;
  }

  @Create
  public abstract T createWiki();

  public T getWiki(String name) {
    Map<String, T> wikis = getWikis();
    return wikis.get(name);
  }

  public Collection<T> getAllWikis() {
    Map<String, T> wikis = getWikis();
    return wikis.values();
  }

}
