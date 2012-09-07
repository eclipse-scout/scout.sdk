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
package org.eclipse.scout.sdk.ui.view.outline.pages;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchyChangedListener;

public class PageDirtyListener implements ITypeHierarchyChangedListener {
  private AbstractPage m_page;

  public PageDirtyListener(AbstractPage page) {
    m_page = page;
  }

  @Override
  public void handleEvent(int eventType, IType type) {
    switch (eventType) {
      case POST_TYPE_REMOVING:
      case POST_TYPE_ADDING:
      case POST_TYPE_CHANGED:
        m_page.markStructureDirty();
        break;
    }
  }

  public void typeChanged(IType type, int eventType, IJavaElement modification) {
    m_page.markStructureDirty();
  }

  public void innerTypeChanged(IType type, IType innerType, int eventType, IJavaElement modification) {
    if (innerType != null && innerType.getDeclaringType() == type) {
      m_page.markStructureDirty();
    }
  }

  public void subTypeHierarchyChanged(IType type) {
    m_page.markStructureDirty();
  }

}
