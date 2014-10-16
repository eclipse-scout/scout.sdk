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
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.internal.ScoutSdk;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.JavaElementFormatOperation;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 *
 */
public class WellformClientBundleOperation implements IOperation {

  private final Set<? extends IScoutBundle> m_bundles;

  public WellformClientBundleOperation(Set<? extends IScoutBundle> bundles) {
    m_bundles = bundles;
  }

  @Override
  public String getOperationName() {
    StringBuilder builder = new StringBuilder();
    builder.append("Wellform client bundle(s)...");
    return builder.toString();
  }

  @Override
  public void validate() {
    if (m_bundles == null) {
      throw new IllegalArgumentException("bundles can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    for (IScoutBundle bundle : m_bundles) {
      if (bundle.hasType(IScoutBundle.TYPE_CLIENT) && !bundle.isBinary()) {
        HashSet<IType> allTypes = new HashSet<IType>();
        IPackageFragmentRoot[] packageFragmentRoots = bundle.getJavaProject().getPackageFragmentRoots();
        for (IPackageFragmentRoot pr : packageFragmentRoots) {
          if (!pr.isReadOnly()) {
            collectTypesRec(pr, allTypes);
          }
        }
        wellformDesktop(allTypes, bundle, monitor, workingCopyManager);
        wellformDesktopExtension(allTypes, bundle, monitor, workingCopyManager);
        wellformClientSession(allTypes, bundle, monitor, workingCopyManager);
        wellformForms(allTypes, bundle, monitor, workingCopyManager);
        wellformSearchForms(allTypes, bundle, monitor, workingCopyManager);
        wellformWizards(allTypes, bundle, monitor, workingCopyManager);
        wellformLookupCalls(allTypes, bundle, monitor, workingCopyManager);
        wellformPages(allTypes, bundle, monitor, workingCopyManager);
        wellformOutlines(allTypes, bundle, monitor, workingCopyManager);
        for (IType t : allTypes) {
          if (t.exists()) {
            JavaElementFormatOperation op = new JavaElementFormatOperation(t, true);
            op.run(monitor, workingCopyManager);
          }
        }
      }
    }
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

  protected void wellformClientSession(Set<IType> types, IScoutBundle bundle, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    Set<IType> clientSessions = ScoutTypeUtility.getClientSessionTypes(bundle.getJavaProject());
    WellformScoutTypeOperation op = new WellformScoutTypeOperation(clientSessions, true);
    try {
      op.run(monitor, workingCopyManager);
      for (IType t : clientSessions) {
        types.remove(t);
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform client sessions of bundle '" + bundle.getSymbolicName() + "'.", e);
    }
  }

  protected void wellformDesktop(Set<IType> types, IScoutBundle bundle, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    IType idesktop = TypeUtility.getType(IRuntimeClasses.IDesktop);
    Set<IType> desktops = TypeUtility.getPrimaryTypeHierarchy(idesktop).getAllSubtypes(idesktop, ScoutTypeFilters.getClassesInScoutBundles(bundle));
    WellformScoutTypeOperation op = new WellformScoutTypeOperation(desktops, true);
    try {
      op.run(monitor, workingCopyManager);
      for (IType desktop : desktops) {
        types.remove(desktop);
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform desktops of bundle '" + bundle.getSymbolicName() + "'.", e);
    }
  }

  protected void wellformDesktopExtension(Set<IType> types, IScoutBundle bundle, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    IType iDesktop = TypeUtility.getType(IRuntimeClasses.IDesktopExtension);
    Set<IType> desktops = TypeUtility.getPrimaryTypeHierarchy(iDesktop).getAllSubtypes(iDesktop, ScoutTypeFilters.getClassesInScoutBundles(bundle));
    WellformScoutTypeOperation op = new WellformScoutTypeOperation(desktops, true);
    try {
      op.run(monitor, workingCopyManager);
      for (IType desktop : desktops) {
        types.remove(desktop);
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform desktops of bundle '" + bundle.getSymbolicName() + "'.", e);
    }
  }

  protected void wellformForms(Set<IType> types, IScoutBundle bundle, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    WellformFormsOperation op = new WellformFormsOperation(bundle);
    try {
      op.run(monitor, workingCopyManager);
      if (op.getForms() != null) {
        for (IType type : op.getForms()) {
          types.remove(type);
        }
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform forms of bundle '" + bundle.getSymbolicName() + "'.", e);
    }
  }

  protected void wellformSearchForms(Set<IType> types, IScoutBundle bundle, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    WellformSearchFormsOperation op = new WellformSearchFormsOperation(bundle);
    try {
      op.run(monitor, workingCopyManager);
      if (op.getSearchForms() != null) {
        for (IType type : op.getSearchForms()) {
          types.remove(type);
        }
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform search forms of bundle '" + bundle.getSymbolicName() + "'.", e);
    }
  }

  protected void wellformWizards(Set<IType> types, IScoutBundle bundle, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    WellformWizardsOperation op = new WellformWizardsOperation(bundle);
    try {
      op.run(monitor, workingCopyManager);
      if (op.getWizards() != null) {
        for (IType type : op.getWizards()) {
          types.remove(type);
        }
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform wizards of bundle '" + bundle.getSymbolicName() + "'.", e);
    }
  }

  protected void wellformLookupCalls(Set<IType> types, IScoutBundle bundle, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    WellformLookupCallsOperation op = new WellformLookupCallsOperation(bundle);
    try {
      op.run(monitor, workingCopyManager);
      if (op.getLookupCalls() != null) {
        for (IType type : op.getLookupCalls()) {
          types.remove(type);
        }
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform lookup calls of bundle '" + bundle.getSymbolicName() + "'.", e);
    }
  }

  protected void wellformPages(Set<IType> types, IScoutBundle bundle, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    WellformPagesOperation op = new WellformPagesOperation(bundle);
    try {
      op.run(monitor, workingCopyManager);
      if (op.getPages() != null) {
        for (IType type : op.getPages()) {
          types.remove(type);
        }
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform pages of bundle '" + bundle.getSymbolicName() + "'.", e);
    }
  }

  protected void wellformOutlines(Set<IType> types, IScoutBundle bundle, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    WellformOutlinesOperation op = new WellformOutlinesOperation(bundle);
    try {
      op.run(monitor, workingCopyManager);
      if (op.getOutlines() != null) {
        for (IType type : op.getOutlines()) {
          types.remove(type);
        }
      }
    }
    catch (Exception e) {
      ScoutSdk.logWarning("could not wellform outlines of bundle '" + bundle.getSymbolicName() + "'.", e);
    }
  }
}
