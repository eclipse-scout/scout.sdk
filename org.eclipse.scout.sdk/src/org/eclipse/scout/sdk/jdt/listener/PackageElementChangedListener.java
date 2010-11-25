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
package org.eclipse.scout.sdk.jdt.listener;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * listen on a package for added / removed types
 */
public abstract class PackageElementChangedListener extends ElementChangedListenerEx {

  private IPackageFragment[] m_packages;

  public PackageElementChangedListener(IPackageFragment pck) {
    this(new IPackageFragment[]{pck});
  }

  public PackageElementChangedListener(IPackageFragment[] pcks) {
    m_packages = pcks;
  }

  @Override
  protected boolean visitPackageModify(int flags, IJavaElement e, CompilationUnit ast) {
    if (e.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
      boolean retVal = true;
      for (IPackageFragment p : m_packages) {
        if (p.getPath().isPrefixOf(e.getPath())) {
          packageContentChanged(flags, e);
          retVal = false;
        }
      }
      return retVal;
    }
    else {
      return true;
    }
  }

  @Override
  protected boolean visit(int kind, int flags, IJavaElement e, CompilationUnit ast) {
    if (e.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
      boolean retVal = true;
      for (IPackageFragment p : m_packages) {
        if (p.getPath().isPrefixOf(e.getPath())) {
          packageContentChanged(flags, e);
          retVal = false;
        }
      }
      return retVal;
    }
    else {
      return true;
    }
  }

  public abstract void packageContentChanged(int flags, IJavaElement e);

}
