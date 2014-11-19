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

import java.security.Permission;
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
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.jobs.AbstractWorkspaceBlockingJob;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.ui.dialog.IMemberSelectionChangedListener;
import org.eclipse.scout.sdk.ui.dialog.MemberSelectionDialog;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link FormDeleteExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 08.10.2014
 */
public class FormDeleteExecutor extends AbstractExecutor {

  private IType m_formType;
  private IType m_formData;
  private Set<IType> m_processServiceInterface;
  private Set<IType> m_processServiceImplementation;
  private MemberSelectionDialog m_confirmDialog;

  @Override
  public boolean canRun(IStructuredSelection selection) {
    m_formType = UiUtility.getTypeFromSelection(selection);
    return isEditable(m_formType);
  }

  @Override
  public Object run(final Shell shell, final IStructuredSelection selection, final ExecutionEvent event) {
    new AbstractWorkspaceBlockingJob("Collect elements to delete") {
      @Override
      protected void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
        runAsync(shell, selection, event);
      }
    }.schedule();
    return null;
  }

  protected void runAsync(final Shell shell, final IStructuredSelection selection, final ExecutionEvent event) {
    final Set<IMember> members = new LinkedHashSet<IMember>();
    final Set<IMember> selectedMembers = new LinkedHashSet<IMember>();

    collectAffectedMembers(members, selectedMembers, selection);

    Display.getDefault().asyncExec(new Runnable() {
      @Override
      public void run() {
        m_confirmDialog = new MemberSelectionDialog(shell, Texts.get("Action_deleteTypeX", m_formType.getElementName()));
        m_confirmDialog.addMemberSelectionListener(new P_SelectionValidationListener());
        m_confirmDialog.setMembers(members);
        m_confirmDialog.setSelectedMembers(selectedMembers);

        if (m_confirmDialog.open() == Dialog.OK) {
          JavaElementDeleteOperation op = new JavaElementDeleteOperation() {
            @Override
            protected void deleteMember(IJavaElement member, Set<ICompilationUnit> icuForOrganizeImports, IProgressMonitor m, IWorkingCopyManager mgr) throws CoreException {
              if (member instanceof IType && m_processServiceInterface.contains(member)) {
                ScoutUtility.unregisterServiceProxy((IType) member);
              }
              if (member instanceof IType && m_processServiceImplementation.contains(member)) {
                ScoutUtility.unregisterServiceImplementation((IType) member);
              }
              super.deleteMember(member, icuForOrganizeImports, m, mgr);
            }
          };
          op.setMembers(m_confirmDialog.getSelectedMembers());

          new OperationJob(op).schedule();
        }
      }
    });
  }

  protected void collectAffectedMembers(Set<IMember> members, Set<IMember> selectedMembers, IStructuredSelection selection) {
    // form
    members.add(m_formType);
    selectedMembers.add(m_formType);

    // form data
    try {
      m_formData = ScoutTypeUtility.findDtoForForm(m_formType);
    }
    catch (JavaModelException e) {
      ScoutSdkUi.logError(e);
    }
    if (TypeUtility.exists(m_formData)) {
      members.add(m_formData);
      selectedMembers.add(m_formData);
    }

    // find process service interfaces
    final String formName = m_formType.getElementName().replaceAll("^(.*)" + SdkProperties.SUFFIX_FORM + "$", "$1");
    IType iService = TypeUtility.getType(IRuntimeClasses.IService);
    m_processServiceInterface = TypeUtility.getInterfacesOnClasspath(iService, m_formType.getJavaProject(), TypeFilters.getRegexSimpleNameFilter("I" + formName + "(Process)?" + SdkProperties.SUFFIX_SERVICE, 0));
    members.addAll(m_processServiceInterface);

    // process service implementations
    m_processServiceImplementation = new HashSet<IType>();
    for (IType serviceInterface : m_processServiceInterface) {
      Set<IType> implementations = TypeUtility.getPrimaryTypeHierarchy(serviceInterface).getAllSubtypes(serviceInterface, TypeFilters.getClassFilter(), TypeComparators.getTypeNameComparator());
      m_processServiceImplementation.addAll(implementations);
    }
    members.addAll(m_processServiceImplementation);

    // find Permissions
    String permissionRegex = m_formType.getElementName().replaceAll("^(.*)" + SdkProperties.SUFFIX_FORM + "$", "(Create|Read|Update)$1" + SdkProperties.SUFFIX_PERMISSION);
    IType permission = TypeUtility.getType(Permission.class.getName());
    Set<IType> permissions = TypeUtility.getClassesOnClasspath(permission, m_formType.getJavaProject(), TypeFilters.getRegexSimpleNameFilter(permissionRegex, 0));
    members.addAll(permissions);
  }

  private class P_SelectionValidationListener implements IMemberSelectionChangedListener {
    @Override
    public void handleSelectionChanged(Set<? extends IMember> selection) {
      m_confirmDialog.setMessage("");
      boolean canOk = true;
      if (selection == null || selection.size() == 0) {
        canOk = false;
      }
      else {
        if (m_formType != null && m_formData != null && (selection.contains(m_formType) != selection.contains(m_formData))) {
          m_confirmDialog.setMessage(Texts.get("FormDataDeleteWithForm", m_formData.getElementName()), IMessageProvider.WARNING);
        }
      }
      m_confirmDialog.getOkButton().setEnabled(canOk);
    }
  }
}
