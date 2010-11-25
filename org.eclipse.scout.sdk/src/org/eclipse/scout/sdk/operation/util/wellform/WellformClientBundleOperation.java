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
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.util.JavaElementFormatOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;

/**
 *
 */
public class WellformClientBundleOperation implements IOperation {

  private final IScoutBundle m_bundle;

  public WellformClientBundleOperation(IScoutBundle bundle) {
    m_bundle = bundle;
  }

  @Override
  public String getOperationName() {
    StringBuilder builder = new StringBuilder();
    builder.append("Wellform '" + getBundle().getBundleName() + "'...");
    return builder.toString();
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getBundle().getType() != IScoutBundle.BUNDLE_CLIENT) {
      throw new IllegalArgumentException("bundle must be a client bundle.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    HashSet<IType> allTypes = new HashSet<IType>();
    IPackageFragmentRoot[] packageFragmentRoots = getBundle().getJavaProject().getPackageFragmentRoots();
    for (IPackageFragmentRoot pr : packageFragmentRoots) {
      if (!pr.isReadOnly()) {
        collectTypesRec(pr, allTypes);
      }
    }
    wellformDesktop(allTypes, monitor, workingCopyManager);
    wellformClientSession(allTypes, monitor, workingCopyManager);
    wellformForms(allTypes, monitor, workingCopyManager);
    wellformSearchForms(allTypes, monitor, workingCopyManager);
    wellformWizards(allTypes, monitor, workingCopyManager);
    wellformLookupCalls(allTypes, monitor, workingCopyManager);
    wellformPages(allTypes, monitor, workingCopyManager);
    wellformOutlines(allTypes, monitor, workingCopyManager);
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

  protected void wellformClientSession(Set<IType> types, IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) {
    IType iClientSessions = ScoutSdk.getType(RuntimeClasses.IClientSession);
    IType[] clientSessions = ScoutSdk.getPrimaryTypeHierarchy(iClientSessions).getAllSubtypes(iClientSessions, TypeFilters.getClassesInProject(getBundle().getJavaProject()));
    WellformScoutTypeOperation op = new WellformScoutTypeOperation(clientSessions, true);
    try {
      op.run(monitor, workingCopyManager);
      for (IType t : clientSessions) {
        types.remove(t);
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform client sessions of bundle '" + getBundle().getBundleName() + "'.", e);
    }
  }

  protected void wellformDesktop(Set<IType> types, IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) {
    IType idesktop = ScoutSdk.getType(RuntimeClasses.IDesktop);
    IType[] desktops = ScoutSdk.getPrimaryTypeHierarchy(idesktop).getAllSubtypes(idesktop, TypeFilters.getClassesInProject(getBundle().getJavaProject()));
    WellformScoutTypeOperation op = new WellformScoutTypeOperation(desktops, true);
    try {
      op.run(monitor, workingCopyManager);
      for (IType desktop : desktops) {
        types.remove(desktop);
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform desktops of bundle '" + getBundle().getBundleName() + "'.", e);
    }
  }

  protected void wellformForms(Set<IType> types, IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) {
    WellformFormsOperation op = new WellformFormsOperation(getBundle());
    try {
      op.run(monitor, workingCopyManager);
      if (op.getForms() != null) {
        for (IType type : op.getForms()) {
          types.remove(type);
        }
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform forms of bundle '" + getBundle().getBundleName() + "'.", e);
    }
  }

  protected void wellformSearchForms(Set<IType> types, IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) {
    WellformSearchFormsOperation op = new WellformSearchFormsOperation(getBundle());
    try {
      op.run(monitor, workingCopyManager);
      if (op.getSearchForms() != null) {
        for (IType type : op.getSearchForms()) {
          types.remove(type);
        }
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform search forms of bundle '" + getBundle().getBundleName() + "'.", e);
    }
  }

  protected void wellformWizards(Set<IType> types, IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) {
    WellformWizardsOperation op = new WellformWizardsOperation(getBundle());
    try {
      op.run(monitor, workingCopyManager);
      if (op.getWizards() != null) {
        for (IType type : op.getWizards()) {
          types.remove(type);
        }
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform wizards of bundle '" + getBundle().getBundleName() + "'.", e);
    }
  }

  protected void wellformLookupCalls(Set<IType> types, IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) {
    WellformLookupCallsOperation op = new WellformLookupCallsOperation(getBundle());
    try {
      op.run(monitor, workingCopyManager);
      if (op.getLookupCalls() != null) {
        for (IType type : op.getLookupCalls()) {
          types.remove(type);
        }
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform lookup calls of bundle '" + getBundle().getBundleName() + "'.", e);
    }
  }

  protected void wellformPages(Set<IType> types, IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) {
    WellformPagesOperation op = new WellformPagesOperation(getBundle());
    try {
      op.run(monitor, workingCopyManager);
      if (op.getPages() != null) {
        for (IType type : op.getPages()) {
          types.remove(type);
        }
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform pages of bundle '" + getBundle().getBundleName() + "'.", e);
    }
  }

  protected void wellformOutlines(Set<IType> types, IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) {
    WellformOutlinesOperation op = new WellformOutlinesOperation(getBundle());
    try {
      op.run(monitor, workingCopyManager);
      if (op.getOutlines() != null) {
        for (IType type : op.getOutlines()) {
          types.remove(type);
        }
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform outlines of bundle '" + getBundle().getBundleName() + "'.", e);
    }
  }
}
