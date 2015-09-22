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
package org.eclipse.scout.sdk.operation.util.wellform;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.JavaElementFormatOperation;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 *
 */
public class WellformServerBundleOperation implements IOperation {

  private final IScoutBundle m_bundle;

  public WellformServerBundleOperation(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  @Override
  public String getOperationName() {
    StringBuilder builder = new StringBuilder();
    builder.append("Wellform '" + getBundle().getSymbolicName() + "'...");
    return builder.toString();
  }

  @Override
  public void validate() {
    if (getBundle() == null) {
      throw new IllegalArgumentException("Bundle can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    HashSet<IType> allTypes = new HashSet<IType>();
    IPackageFragmentRoot[] packageFragmentRoots = getBundle().getJavaProject().getPackageFragmentRoots();
    for (IPackageFragmentRoot pr : packageFragmentRoots) {
      if (!pr.isReadOnly()) {
        collectTypesRec(pr, allTypes);
      }
    }
    for (IType t : allTypes) {
      if (t.exists()) {
        JavaElementFormatOperation op = new JavaElementFormatOperation(t, true);
        op.run(monitor, workingCopyManager);
      }
    }
  }

  public IScoutBundle getBundle() {
    return m_bundle;
  }

  protected void collectTypesRec(IJavaElement element, Set<IType> collector) throws JavaModelException {
    switch (element.getElementType()) {
      case IJavaElement.PACKAGE_FRAGMENT_ROOT:
      case IJavaElement.PACKAGE_FRAGMENT:
      case IJavaElement.COMPILATION_UNIT:
        for (IJavaElement e : ((IParent) element).getChildren()) {
          collectTypesRec(e, collector);
        }
        break;
      case IJavaElement.TYPE:
        collector.add((IType) element);
        break;
    }
  }

}