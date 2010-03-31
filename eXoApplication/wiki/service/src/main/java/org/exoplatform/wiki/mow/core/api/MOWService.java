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
package org.exoplatform.wiki.mow.core.api;

import org.chromattic.api.Chromattic;
import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class MOWService {

  private ChromatticManager   chromatticManager;

  private ChromatticLifeCycle chromatticLifeCycle;

  public MOWService(ChromatticManager chromatticManager) {
    this.chromatticManager = chromatticManager;
    this.chromatticLifeCycle = chromatticManager.getLifeCycle("wiki");
  }

  public ModelImpl getModel() {
    Chromattic chromattic = chromatticLifeCycle.getChromattic();
    ChromatticSession chromeSession = chromattic.openSession();
    return new ModelImpl(chromeSession);
  }
}
