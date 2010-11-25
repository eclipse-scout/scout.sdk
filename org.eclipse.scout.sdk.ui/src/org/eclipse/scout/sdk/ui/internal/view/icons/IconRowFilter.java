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
package org.eclipse.scout.sdk.ui.internal.view.icons;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.scout.sdk.icon.ScoutIconDesc;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 * Accept only rows with icons of the current bc project, hide inherited rows
 */
public class IconRowFilter extends ViewerFilter {
  private IScoutBundle m_sharedBundle;

  public void setScoutBundle(IScoutBundle sharedBundle) {
    m_sharedBundle = sharedBundle;
  }

  @Override
  public boolean select(Viewer viewer, Object parentElement, Object element) {
    if (element instanceof ScoutIconDesc) {
      ScoutIconDesc icon = (ScoutIconDesc) element;
      return !icon.isInherited();
    }
    return true;
  }
}
