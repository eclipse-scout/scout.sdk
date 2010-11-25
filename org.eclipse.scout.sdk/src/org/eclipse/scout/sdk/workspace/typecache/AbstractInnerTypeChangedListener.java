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
package org.eclipse.scout.sdk.workspace.typecache;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IType;

/**
 *
 */
public abstract class AbstractInnerTypeChangedListener implements IElementChangedListener {

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
      if (e != null && e.getElementType() == IJavaElement.TYPE) {
        IType type = (IType) e;
        switch (kind) {
          case IJavaElementDelta.ADDED:
          case IJavaElementDelta.CHANGED:
          case IJavaElementDelta.REMOVED:
            innerTypeChanged(type.getDeclaringType(), type);
            break;
          default:

        }

      }
    }
  }

  public abstract void innerTypeChanged(IType parentType, IType innerType);

}
