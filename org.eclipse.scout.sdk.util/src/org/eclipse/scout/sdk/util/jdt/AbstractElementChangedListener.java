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
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.scout.sdk.util.internal.typecache.JavaResourceChangedEmitter;
import org.eclipse.scout.sdk.util.jdt.finegrained.FineGrainedJavaElementDeltaManager;

/**
 * WARNING: JDT is not correctly reporting changes in ITypes and IMethods
 * JDT stops reporting at ICompilationUnit workingCopyManager on content-only changes, whereas add/remove changes are
 * reported down to IMethod workingCopyManager.
 *
 * @Note never add the IJavaElementDelta.F_FINE_GRAINED flag to the CHANGED_FLAG_MASK.
 *       Therefore this class is taking this into account and tries to report as precise as possible down to fine
 *       grained levels
 */
public abstract class AbstractElementChangedListener implements IElementChangedListener {

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
          if (!visit(kind, flags, e, ast)) {
            return false;
          }
          if (!visitAdd(flags, e, ast)) {
            return false;
          }
          break;
        }
        case IJavaElementDelta.REMOVED: {
          if (!visit(kind, flags, e, ast)) {
            return false;
          }
          if (!visitRemove(flags, e, ast)) {
            return false;
          }
          break;
        }
        case IJavaElementDelta.CHANGED: {
          if (e.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
            if (!visitPackageModify(flags, (IPackageFragment) e, ast)) {
              return false;
            }
          }
          if ((flags & JavaResourceChangedEmitter.CHANGED_FLAG_MASK) != 0) {
            // workaround: try to find out what really changed
            if (e.getElementType() == IJavaElement.COMPILATION_UNIT) {
              for (IJavaElement a : FineGrainedJavaElementDeltaManager.getInstance().getDelta(delta)) {
                if (!visit(kind, flags, a, ast)) {
                  return false;
                }
                if (!visitModify(flags, a, ast)) {
                  return false;
                }
              }
            }
            else {
              if (!visit(kind, flags, e, ast)) {
                return false;
              }
              if (!visitModify(flags, e, ast)) {
                return false;
              }
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
          if (!visitDelta(childDeltas[i], type, ast)) {
            return false;
          }
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
   * @return true if the listener should continue to visit the changes delta tree. false if no further visiting is
   *         desired.
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
   * @return true if the listener should continue to visit the changes delta tree. false if no further visiting is
   *         desired.
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
   * @return true if the listener should continue to visit the changes delta tree. false if no further visiting is
   *         desired.
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
   * @return true if the listener should continue to visit the changes delta tree. false if no further visiting is
   *         desired.
   */
  protected boolean visitModify(int flags, IJavaElement e, CompilationUnit ast) {
    return true;
  }

  /**
   * @param flags
   * @param e
   * @param ast
   * @return true if the listener should continue to visit the changes delta tree. false if no further visiting is
   *         desired.
   */
  protected boolean visitPackageModify(int flags, IPackageFragment e, CompilationUnit ast) {
    return true;
  }
}
