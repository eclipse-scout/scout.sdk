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

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.util.jdt.IJavaResourceChangedListener;
import org.eclipse.scout.sdk.util.jdt.JdtEvent;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;

/**
 * <h3>{@link InnerTypePageDirtyListener}</h3> This listener marks the given page dirty (what means all child pages will
 * be recreated)
 * if:
 * <ul>
 * <li>the changed type is the member of any child page and does not contain the given super type anymore in the
 * supertype hierarchy</li>
 * <li>the changed type is the not member of any child page and does contain the given super type anymore in the
 * supertype hierarchy</li>
 * </ul>
 *
 * @author Andreas Hoegger
 * @since 1.0.8 21.11.2010
 */
public class InnerTypePageDirtyListener implements IJavaResourceChangedListener {
  private final IPage m_page;
  private final IType m_superType;

  public InnerTypePageDirtyListener(IPage page, IType superType) {
    m_page = page;
    m_superType = superType;
  }

  /**
   * @return the page
   */
  public IPage getPage() {
    return m_page;
  }

  /**
   * @return the superType
   */
  public IType getSuperType() {
    return m_superType;
  }

  @Override
  public void handleEvent(JdtEvent event) {
    ITypePage page = null;
    for (IPage p : getPage().getChildren()) {
      if (p instanceof ITypePage) {
        if (((ITypePage) p).getType().equals(event.getElement())) {
          page = (ITypePage) p;
          break;
        }
      }
    }
    if (page != null && (event.getEventType() == JdtEvent.REMOVED || event.getEventType() == JdtEvent.ADDED)) {
      handleChildPagesChanged(event);
      return;
    }
    ITypeHierarchy superTypeHierarchy = event.getSuperTypeHierarchy();
    if (superTypeHierarchy != null) {
      if (page != null) {
        if (!superTypeHierarchy.contains(getSuperType())) {
          handleChildPagesChanged(event);
          return;
        }
      }
      else {
        if (superTypeHierarchy.contains(getSuperType())) {
          handleChildPagesChanged(event);
        }
      }
    }
  }

  protected void handleChildPagesChanged(JdtEvent event) {
    getPage().markStructureDirty();
  }
}
