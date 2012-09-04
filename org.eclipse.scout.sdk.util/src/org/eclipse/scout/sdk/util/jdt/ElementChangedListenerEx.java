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
package org.eclipse.scout.sdk.util.jdt;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.scout.sdk.util.jdt.finegraned.FineGrainedJavaElementDelta;
import org.eclipse.scout.sdk.util.jdt.finegraned.FineGrainedJavaElementDeltaManager;

/**
 * WARNING: JDT is not correctly reporting changes in ITypes and IMethods
 * JDT stops reporting at ICompilationUnit workingCopyManager on content-only changes, whereas add/remove changes are
 * reported down to IMethod workingCopyManager.
 * 
 * @Note never add the IJavaElementDelta.F_FINE_GRAINED flag to the CHANGED_FLAG_MASK.
 *       Therefore this class is taking this into account and tries to report as precise as possible down to fine
 *       grained levels
 */
public class ElementChangedListenerEx implements IElementChangedListener {
  public static final int CHANGED_FLAG_MASK =
      IJavaElementDelta.F_CONTENT |
          IJavaElementDelta.F_MODIFIERS |
          IJavaElementDelta.F_MOVED_FROM |
          IJavaElementDelta.F_MOVED_TO |
          IJavaElementDelta.F_REORDER |
          IJavaElementDelta.F_SUPER_TYPES |
          IJavaElementDelta.F_OPENED |
          IJavaElementDelta.F_CLOSED |
          IJavaElementDelta.F_CATEGORIES |
          IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED |
          IJavaElementDelta.F_ANNOTATIONS;

  @Override
  public void elementChanged(ElementChangedEvent e) {
    CompilationUnit ast = e.getDelta().getCompilationUnitAST();
    visitDelta(e.getDelta(), e.getType(), ast);
  }

  private boolean visitDelta(IJavaElementDelta delta, int type, CompilationUnit ast) {
    int flags = delta.getFlags();
    int kind = delta.getKind();
    IJavaElement e = delta.getElement();
    if (e != null) {
      switch (kind) {
        case IJavaElementDelta.ADDED: {
          if (!visit(kind, flags, e, ast)) return false;
          if (!visitAdd(flags, e, ast)) return false;
          break;
        }
        case IJavaElementDelta.REMOVED: {
          if (!visit(kind, flags, e, ast)) return false;
          if (!visitRemove(flags, e, ast)) return false;
          break;
        }
        case IJavaElementDelta.CHANGED: {
          if (e.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
            if (!visitPackageModify(flags, e, ast)) return false;
          }
          if ((flags & CHANGED_FLAG_MASK) != 0) {
            // workaround: try to find out what really changed
            if (e.getElementType() == IJavaElement.COMPILATION_UNIT) {
              for (FineGrainedJavaElementDelta a : FineGrainedJavaElementDeltaManager.getInstance().getDelta(delta)) {
                if (!visit(kind, flags, a.getElement(), ast)) return false;
                if (!visitModify(flags, a.getElement(), ast)) return false;
              }
            }
            else {
              if (!visit(kind, flags, e, ast)) return false;
              if (!visitModify(flags, e, ast)) return false;
            }
          }
          break;
        }
      }
    }
    // children
    if ((flags & IJavaElementDelta.F_CHILDREN) != 0) {
      IJavaElementDelta[] childDeltas = delta.getAffectedChildren();
      if (childDeltas != null && childDeltas.length > 0) {
        for (int i = 0; i < childDeltas.length; i++) {
          if (!visitDelta(childDeltas[i], type, ast)) return false;
        }
      }
    }
    return true;
  }

  /**
   * @param kind
   *          ADDED, REMOVED, CHANGED
   * @param flags
   *          F_...
   * @param e
   *          IJavaElement
   * @param ast
   *          CompilationUnit AST (can be null)
   */
  protected boolean visit(int kind, int flags, IJavaElement e, CompilationUnit ast) {
    return true;
  }

  /**
   * @param flags
   *          F_...
   * @param e
   *          IJavaElement
   * @param cu
   *          CompilationUnit AST (can be null)
   */
  protected boolean visitAdd(int flags, IJavaElement e, CompilationUnit ast) {
    return true;
  }

  /**
   * @param flags
   *          F_...
   * @param e
   *          IJavaElement
   * @param cu
   *          CompilationUnit AST (can be null)
   */
  protected boolean visitRemove(int flags, IJavaElement e, CompilationUnit ast) {
    return true;
  }

  /**
   * @param flags
   *          F_...
   * @param e
   *          IJavaElement
   * @param cu
   *          CompilationUnit AST (can be null)
   */
  protected boolean visitModify(int flags, IJavaElement e, CompilationUnit ast) {
    return true;
  }

  protected boolean visitPackageModify(int flags, IJavaElement e, CompilationUnit ast) {
    return true;
  }
}
