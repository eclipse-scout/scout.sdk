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
package org.eclipse.scout.sdk.ui.extensions;

import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;

/**
 * <h3>IPageFilterExtension</h3> This interface belongs to the Extension Point
 * <b>org.eclipse.scout.sdk.ui.scoutExplorerExtension</b>
 * and is implement to make some more checks if a page (explorer node) should be added to a given
 * parent page.
 */
public interface IPageFilterExtension {

  /**
   * @param parentPage
   * @return true to add the the page to the given parent page.
   */
  boolean isValidParentPage(IPage parentPage);
}
