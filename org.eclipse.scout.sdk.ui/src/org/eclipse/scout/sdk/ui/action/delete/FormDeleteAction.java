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
package org.eclipse.scout.sdk.ui.action.delete;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.util.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.dialog.IMemberSelectionChangedListener;
import org.eclipse.scout.sdk.ui.dialog.MemberSelectionDialog;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ITypeFilter;
import org.eclipse.scout.sdk.workspace.type.TypeFilters;
import org.eclipse.scout.sdk.workspace.typecache.ICachedTypeHierarchy;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>FormDeleteAction</h3> The ui action to delete a form. If a process service has the same name pattern
 * (e.g. CompanyForm -> (I)CompanyProcessService the user will be asked to delete
 * also the service.
 */
public class FormDeleteAction extends Action {

  private IType m_formType;
  private IType m_formData;
  private IType m_processServiceInterface;
  private IType m_processServiceImplementation;
  private final Shell m_shell;
  private MemberSelectionDialog m_confirmDialog;

  public FormDeleteAction(IType formType, Shell shell) {
    super("Delete Form");
    m_shell = shell;
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_TOOL_DELETE));
    m_formType = formType;
  }

  @Override
  public void run() {
    m_confirmDialog = new MemberSelectionDialog(m_shell, "Delete Form");
    m_confirmDialog.addMemberSelectionListener(new P_SelectionValidationListener());
    List<IMember> members = new ArrayList<IMember>();
    List<IMember> selectedMembers = new ArrayList<IMember>();
    collectAffectedMembers(members, selectedMembers);
    m_confirmDialog.setMembers(members.toArray(new IMember[members.size()]));
    m_confirmDialog.setSelectedMembers(selectedMembers.toArray(new IMember[selectedMembers.size()]));
    if (m_confirmDialog.open() == Dialog.OK) {
      JavaElementDeleteOperation op = new JavaElementDeleteOperation() {
        @Override
        protected void deleteMember(IJavaElement member, IProgressMonitor monitor, IScoutWorkingCopyManager manager) throws CoreException {
          if (m_processServiceInterface != null && member.equals(m_processServiceInterface)) {
            ScoutUtility.unregisterServiceClass(m_processServiceInterface.getJavaProject().getProject(),
                "org.eclipse.scout.rt.client.serviceProxies", "serviceProxy", m_processServiceInterface.getFullyQualifiedName(), null, monitor);
          }
          if (m_processServiceImplementation != null && member.equals(m_processServiceImplementation)) {
            IScoutBundle implementationBundle = ScoutSdk.getScoutWorkspace().getScoutBundle(m_processServiceImplementation.getJavaProject().getProject());
            ScoutUtility.unregisterServiceClass(m_processServiceImplementation.getJavaProject().getProject(),
                "org.eclipse.scout.rt.server.services", "service", m_processServiceImplementation.getFullyQualifiedName(), implementationBundle.getRootPackageName() + ".ServerSession", monitor);
          }
          super.deleteMember(member, monitor, manager);
        }
      };
      op.setMembers(m_confirmDialog.getSelectedMembers());

      OperationJob job = new OperationJob(op);
      job.schedule();
    }
  }

  protected void collectAffectedMembers(List<IMember> members, List<IMember> selectedMembers) {
    members.add(m_formType);
    selectedMembers.add(m_formType);
    // form data
    if (m_formData == null) {
      IType abstractFormData = ScoutSdk.getType(RuntimeClasses.AbstractFormData);
      ICachedTypeHierarchy formDataHierarchy = ScoutSdk.getPrimaryTypeHierarchy(abstractFormData);
      ITypeFilter formDataFilter = TypeFilters.getMultiTypeFilter(TypeFilters.getTypesOnClasspath(getFormType().getJavaProject()), TypeFilters.getClassFilter());
      for (IType candidate : formDataHierarchy.getAllSubtypes(abstractFormData, formDataFilter, null)) {
        if (candidate.getElementName().equals(getFormType().getElementName() + "Data")) {
          m_formData = candidate;
          break;
        }
      }
    }
    if (m_formData != null && m_formData.exists()) {
      members.add(m_formData);
      selectedMembers.add(m_formData);
    }
    // find process service
    IType iService = ScoutSdk.getType(RuntimeClasses.IService);
    ICachedTypeHierarchy serviceHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iService);
    if (m_processServiceInterface == null) {
      String serviceName = getFormType().getElementName().replaceAll("^(.*)" + ScoutIdeProperties.SUFFIX_FORM + "$", "I$1" + ScoutIdeProperties.SUFFIX_PROCESS_SERVICE);
      ITypeFilter serviceFilter = TypeFilters.getMultiTypeFilter(TypeFilters.getTypesOnClasspath(getFormType().getJavaProject()), TypeFilters.getInterfaceFilter());
      for (IType candidate : serviceHierarchy.getAllSubtypes(iService, serviceFilter, null)) {
        if (candidate.getElementName().equals(serviceName)) {
          m_processServiceInterface = candidate;
          break;
        }
      }
    }
    if (m_processServiceInterface != null && m_processServiceImplementation == null) {
      String serviceName = getFormType().getElementName().replaceAll("^(.*)" + ScoutIdeProperties.SUFFIX_FORM + "$", "$1" + ScoutIdeProperties.SUFFIX_PROCESS_SERVICE);

      ITypeFilter serviceFilter = TypeFilters.getMultiTypeFilter(TypeFilters.getInScoutProject(ScoutSdk.getScoutWorkspace().getScoutBundle(getFormType().getJavaProject().getProject()).getScoutProject()), TypeFilters.getClassFilter());
      for (IType candidate : serviceHierarchy.getAllSubtypes(iService, serviceFilter, null)) {
        if (candidate.getElementName().equals(serviceName)) {
          m_processServiceImplementation = candidate;
          break;
        }
      }
    }
    if (m_processServiceInterface != null) {
      members.add(m_processServiceInterface);
    }
    if (m_processServiceImplementation != null) {
      members.add(m_processServiceImplementation);
    }
    // find Permissions
    String permissionRegex = getFormType().getElementName().replaceAll("^(.*)" + ScoutIdeProperties.SUFFIX_FORM + "$", "(Create|Read|Update)$1" + ScoutIdeProperties.SUFFIX_PERMISSION);
    IType permission = ScoutSdk.getType(Permission.class.getName());
    ICachedTypeHierarchy permissionHierarchy = ScoutSdk.getPrimaryTypeHierarchy(permission);
    ITypeFilter permissionFilter = TypeFilters.getMultiTypeFilter(TypeFilters.getTypesOnClasspath(getFormType().getJavaProject()), TypeFilters.getClassFilter());
    for (IType candidate : permissionHierarchy.getAllSubtypes(permission, permissionFilter, null)) {
      if (candidate.getElementName().matches(permissionRegex)) {
        members.add(candidate);
      }
    }

  }

  public IType getFormType() {
    return m_formType;
  }

  private class P_SelectionValidationListener implements IMemberSelectionChangedListener {
    public void handleSelectionChanged(IMember[] selection) {
      m_confirmDialog.setMessage("");
      HashSet<IMember> members = new HashSet<IMember>(Arrays.asList(selection));
      boolean canOk = true;
      if (selection == null || selection.length == 0) {
        canOk = false;
      }
      if (m_processServiceImplementation != null && m_processServiceInterface != null && (members.contains(m_processServiceInterface) != members.contains(m_processServiceImplementation))) {
        m_confirmDialog.setMessage("Process service interface and implementation must have the same selection.", IMessageProvider.ERROR);
        canOk = false;
      }
      if (m_formType != null && m_formData != null && (members.contains(m_formType) != members.contains(m_formData))) {
        m_confirmDialog.setMessage("FormData '" + m_formData.getElementName() + "' should be deleted together with the form.", IMessageProvider.WARNING);
      }
      m_confirmDialog.getOkButton().setEnabled(canOk);

    }
  }
}
