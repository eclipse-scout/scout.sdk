/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.action.library;

import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;

/**
 * <h3>{@link LibraryBundleNewAction}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.8.0 29.02.2012
 */
public class LibraryBundleNewAction extends AbstractScoutHandler {

  /**
   * @param label
   * @param image
   */
  public LibraryBundleNewAction() {
    super(Texts.get("NewLibraryBundlePopup"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.LibrariesAdd));
  }

}
