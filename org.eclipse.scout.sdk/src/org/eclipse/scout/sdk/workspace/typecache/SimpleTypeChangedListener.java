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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.scout.sdk.jdt.listener.ElementChangedListenerEx;

/**
 *
 */
public abstract class SimpleTypeChangedListener extends ElementChangedListenerEx {

  @Override
  protected final boolean visit(int kind, int flags, IJavaElement e, CompilationUnit ast) {
    if (e != null) {
      while (e != null) {
        if (e.getElementType() == IJavaElement.TYPE) {
          handleTypeChanged((IType) e);
          return false;
        }
        e = e.getParent();
      }
    }
    return super.visit(kind, flags, e, ast);
  }

  protected abstract void handleTypeChanged(IType type);

}
