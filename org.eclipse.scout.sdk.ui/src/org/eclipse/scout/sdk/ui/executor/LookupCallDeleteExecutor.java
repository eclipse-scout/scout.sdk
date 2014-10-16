/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.executor;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.ui.dialog.MemberSelectionDialog;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ICachedTypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link LookupCallDeleteExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class LookupCallDeleteExecutor extends AbstractExecutor {

  private MemberSelectionDialog m_confirmDialog;

  private IType m_lookupCall;
  private Set<IType> m_lookupServices;
  private Set<IType> m_lookupServiceInterfaces;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    m_lookupCall = UiUtility.getTypeFromSelection(selection);
    return isEditable(m_lookupCall);
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    m_confirmDialog = new MemberSelectionDialog(shell, Texts.get("DeleteLookupCall"));

    Set<IMember> members = new LinkedHashSet<IMember>();
    Set<IMember> selectedMembers = new LinkedHashSet<IMember>();

    collectAffectedMembers(members, selectedMembers);

    m_confirmDialog.setMembers(members);
    m_confirmDialog.setSelectedMembers(selectedMembers);
    if (m_confirmDialog.open() == Dialog.OK) {
      JavaElementDeleteOperation op = new JavaElementDeleteOperation() {
        @Override
        protected void deleteMember(IJavaElement member, Set<ICompilationUnit> icuForOrganizeImports, IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
          if (member != null && m_lookupServiceInterfaces.contains(member)) {
            ScoutUtility.unregisterServiceProxy((IType) member);
          }
          if (member != null && m_lookupServices.contains(member)) {
            ScoutUtility.unregisterServiceImplementation((IType) member);
          }
          super.deleteMember(member, icuForOrganizeImports, monitor, manager);
        }
      };
      op.setMembers(m_confirmDialog.getSelectedMembers());

      new OperationJob(op).schedule();
    }
    return null;
  }

  protected void collectAffectedMembers(Set<IMember> members, Set<IMember> selectedMembers) {
    members.add(m_lookupCall);
    selectedMembers.add(m_lookupCall);

    IType iService = TypeUtility.getType(IRuntimeClasses.ILookupService);
    Set<IType> types = new HashSet<IType>(); // all types (interfaces and implementations together)

    // find lookup service interfaces that matches the lookup call name
    ICachedTypeHierarchy serviceHierarchy = TypeUtility.getPrimaryTypeHierarchy(iService);
    String serviceIfcName = m_lookupCall.getElementName().replaceAll("^(.*)" + SdkProperties.SUFFIX_LOOKUP_CALL + "$", "I$1" + SdkProperties.SUFFIX_LOOKUP_SERVICE);
    ITypeFilter serviceIfcFilter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getInterfaceFilter(), TypeFilters.getInWorkspaceFilter(),
        TypeFilters.getElementNameFilter(serviceIfcName), TypeFilters.getTypesOnClasspath(m_lookupCall.getJavaProject()));
    Set<IType> allInterfaces = serviceHierarchy.getAllSubtypes(iService, serviceIfcFilter);
    types.addAll(allInterfaces);
    m_lookupServiceInterfaces = allInterfaces;

    // find lookup service implementations that matches the lookup call name
    String serviceName = m_lookupCall.getElementName().replaceAll("^(.*)" + SdkProperties.SUFFIX_LOOKUP_CALL + "$", "$1" + SdkProperties.SUFFIX_LOOKUP_SERVICE);
    IScoutBundle sharedBundle = ScoutTypeUtility.getScoutBundle(m_lookupCall);
    Set<? extends IScoutBundle> visibleServers = sharedBundle.getChildBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SERVER), true);
    ITypeFilter serviceFilter = TypeFilters.getMultiTypeFilterAnd(TypeFilters.getClassFilter(), TypeFilters.getInWorkspaceFilter(),
        TypeFilters.getElementNameFilter(serviceName), ScoutTypeFilters.getInScoutBundles(visibleServers));
    Set<IType> allImplementations = serviceHierarchy.getAllSubtypes(iService, serviceFilter, null);
    types.addAll(allImplementations);
    m_lookupServices = allImplementations;

    // add all implementations of the interfaces
    for (IType ifc : allInterfaces) {
      Set<IType> all = TypeUtility.getTypeHierarchy(ifc).getAllSubtypes(ifc, TypeFilters.getInWorkspaceFilter());
      for (IType candidate : all) {
        types.add(candidate);
        if (isInterface(candidate)) {
          m_lookupServiceInterfaces.add(candidate);
        }
        else {
          m_lookupServices.add(candidate);
        }
      }
    }

    members.addAll(types);
  }

  private static boolean isInterface(IType t) {
    try {
      return t.isInterface();
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError("Unable to check if type '" + t.getFullyQualifiedName() + "' is an interface or not.", e);
      return false;
    }
  }
}
