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
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.JavaElementFormatOperation;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;

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
    builder.append("Wellform '" + getBundle().getSymbolicName() + "'...");
    return builder.toString();
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getBundle() == null) {
      throw new IllegalArgumentException("Bundle can not be null.");
    }
    if (!getBundle().getType().equals(IScoutBundle.TYPE_CLIENT)) {
      throw new IllegalArgumentException("Bundle must be a client bundle.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    HashSet<IType> allTypes = new HashSet<IType>();
    IPackageFragmentRoot[] packageFragmentRoots = getBundle().getJavaProject().getPackageFragmentRoots();
    for (IPackageFragmentRoot pr : packageFragmentRoots) {
      if (!pr.isReadOnly()) {
        collectTypesRec(pr, allTypes);
      }
    }
    wellformDesktop(allTypes, monitor, workingCopyManager);
    wellformDesktopExtension(allTypes, monitor, workingCopyManager);
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

  protected void wellformClientSession(Set<IType> types, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    IType iClientSessions = TypeUtility.getType(RuntimeClasses.IClientSession);
    IType[] clientSessions = TypeUtility.getPrimaryTypeHierarchy(iClientSessions).getAllSubtypes(iClientSessions, ScoutTypeFilters.getTypesInScoutBundles(getBundle()));
    WellformScoutTypeOperation op = new WellformScoutTypeOperation(clientSessions, true);
    try {
      op.run(monitor, workingCopyManager);
      for (IType t : clientSessions) {
        types.remove(t);
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform client sessions of bundle '" + getBundle().getSymbolicName() + "'.", e);
    }
  }

  protected void wellformDesktop(Set<IType> types, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    IType idesktop = TypeUtility.getType(RuntimeClasses.IDesktop);
    IType[] desktops = TypeUtility.getPrimaryTypeHierarchy(idesktop).getAllSubtypes(idesktop, ScoutTypeFilters.getTypesInScoutBundles(getBundle()));
    WellformScoutTypeOperation op = new WellformScoutTypeOperation(desktops, true);
    try {
      op.run(monitor, workingCopyManager);
      for (IType desktop : desktops) {
        types.remove(desktop);
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform desktops of bundle '" + getBundle().getSymbolicName() + "'.", e);
    }
  }

  protected void wellformDesktopExtension(Set<IType> types, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    IType idesktop = TypeUtility.getType(RuntimeClasses.IDesktopExtension);
    IType[] desktops = TypeUtility.getPrimaryTypeHierarchy(idesktop).getAllSubtypes(idesktop, ScoutTypeFilters.getTypesInScoutBundles(getBundle()));
    WellformScoutTypeOperation op = new WellformScoutTypeOperation(desktops, true);
    try {
      op.run(monitor, workingCopyManager);
      for (IType desktop : desktops) {
        types.remove(desktop);
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform desktops of bundle '" + getBundle().getSymbolicName() + "'.", e);
    }
  }

  protected void wellformForms(Set<IType> types, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
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
      ScoutSdk.logWarning("could not wellform forms of bundle '" + getBundle().getSymbolicName() + "'.", e);
    }
  }

  protected void wellformSearchForms(Set<IType> types, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
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
      ScoutSdk.logWarning("could not wellform search forms of bundle '" + getBundle().getSymbolicName() + "'.", e);
    }
  }

  protected void wellformWizards(Set<IType> types, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
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
      ScoutSdk.logWarning("could not wellform wizards of bundle '" + getBundle().getSymbolicName() + "'.", e);
    }
  }

  protected void wellformLookupCalls(Set<IType> types, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
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
      ScoutSdk.logWarning("could not wellform lookup calls of bundle '" + getBundle().getSymbolicName() + "'.", e);
    }
  }

  protected void wellformPages(Set<IType> types, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
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
      ScoutSdk.logWarning("could not wellform pages of bundle '" + getBundle().getSymbolicName() + "'.", e);
    }
  }

  protected void wellformOutlines(Set<IType> types, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
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
      ScoutSdk.logWarning("could not wellform outlines of bundle '" + getBundle().getSymbolicName() + "'.", e);
    }
  }
}
