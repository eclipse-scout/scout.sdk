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
package org.eclipse.scout.sdk.ui.type;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;

/**
 * <h3>PackageContentChangedListener</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 19.07.2010
 */
public class PackageContentChangedListener implements IElementChangedListener {

  public static final int CHANGED_FLAG_MASK =
      IJavaElementDelta.F_CONTENT |
          IJavaElementDelta.F_CHILDREN |
          IJavaElementDelta.F_MODIFIERS |
          IJavaElementDelta.F_MOVED_FROM |
          IJavaElementDelta.F_MOVED_TO |
          IJavaElementDelta.F_REORDER |
          IJavaElementDelta.F_OPENED |
          IJavaElementDelta.F_CLOSED |
          IJavaElementDelta.F_CATEGORIES;

  private final IPage m_page;
  private IPackageFragment m_pck;

  public PackageContentChangedListener(IPage page, IPackageFragment pck) {
    m_page = page;
    m_pck = pck;

  }

  @Override
  public final void elementChanged(ElementChangedEvent event) {
    visitDelta(event.getDelta(), event.getType());
  }

  private void visitDelta(IJavaElementDelta delta, int eventType) {
    int flags = delta.getFlags();
    int kind = delta.getKind();
    // children
    if ((flags & IJavaElementDelta.F_CHILDREN) != 0) {
      IJavaElementDelta[] childDeltas = delta.getAffectedChildren();
      if (childDeltas != null && childDeltas.length > 0) {
        for (int i = 0; i < childDeltas.length; i++) {
          visitDelta(childDeltas[i], eventType);
        }
      }
    }
    else {
      IJavaElement e = delta.getElement();
      if (e != null) {
        switch (e.getElementType()) {
          case IJavaElement.COMPILATION_UNIT:
            if (kind == IJavaElementDelta.REMOVED) {
              ICompilationUnit icu = (ICompilationUnit) e;
              IJavaElement parent = icu.getParent();
              if (parent.getElementType() == IJavaElement.PACKAGE_FRAGMENT && parent.equals(m_pck)) {
                m_page.markStructureDirty();
              }
            }
            break;
          case IJavaElement.TYPE:
            handleTypeChanged((IType) e, flags, kind);
            break;
          case IJavaElement.PACKAGE_FRAGMENT:
            getPage().markStructureDirty();
            break;

        }
      }
    }

  }

  protected void handleTypeChanged(IType type, int flags, int kind) {
    switch (kind) {
      case IJavaElementDelta.ADDED:
      case IJavaElementDelta.CHANGED:
      case IJavaElementDelta.REMOVED:
        if (type.getPackageFragment().equals(m_pck)) {
          getPage().markStructureDirty();
        }
        break;
      default:

    }
  }

  /**
   * @return the page
   */
  public IPage getPage() {
    return m_page;
  }

}
