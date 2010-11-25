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
 * <h3>LookupCallDeleteAction</h3> The ui action to delete a lookup call. If a process service has the same name pattern
 * (e.g. CompanyLookupCall -> (I)CompanyLookupService the user will be asked to delete
 * also the service.
 */
public class LookupCallDeleteAction extends Action {

  private IType m_lookupCall;
  // action members
  private IType m_lookupService;
  private IType m_lookupServiceInterface;
  private final Shell m_shell;
  private MemberSelectionDialog m_confirmDialog;

  public LookupCallDeleteAction(IType lookupCall, Shell shell) {
    super("Delete Lookup Call");
    m_shell = shell;
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_TOOL_DELETE));
    m_lookupCall = lookupCall;
  }

  @Override
  public void run() {
    m_confirmDialog = new MemberSelectionDialog(m_shell, "Delete Lookup Call");
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
          if (m_lookupServiceInterface != null && member.equals(m_lookupServiceInterface)) {
            ScoutUtility.unregisterServiceClass(m_lookupServiceInterface.getJavaProject().getProject(),
                "org.eclipse.scout.rt.client.serviceProxies", "serviceProxy", m_lookupServiceInterface.getFullyQualifiedName(), null, monitor);
          }
          if (m_lookupService != null && member.equals(m_lookupService)) {
            IScoutBundle implementationBundle = ScoutSdk.getScoutWorkspace().getScoutBundle(m_lookupService.getJavaProject().getProject());
            ScoutUtility.unregisterServiceClass(m_lookupService.getJavaProject().getProject(),
                "org.eclipse.scout.rt.server.services", "service", m_lookupService.getFullyQualifiedName(), implementationBundle.getRootPackageName() + ".ServerSession", monitor);
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
    members.add(getLookupCall());
    selectedMembers.add(getLookupCall());
    // find lookup service
    IType iService = ScoutSdk.getType(RuntimeClasses.ILookupService);
    ICachedTypeHierarchy serviceHierarchy = ScoutSdk.getPrimaryTypeHierarchy(iService);
    if (m_lookupServiceInterface == null) {
      String serviceName = getLookupCall().getElementName().replaceAll("^(.*)" + ScoutIdeProperties.SUFFIX_LOOKUP_CALL + "$", "I$1" + ScoutIdeProperties.SUFFIX_LOOKUP_SERVICE);
      ITypeFilter serviceFilter = TypeFilters.getMultiTypeFilter(TypeFilters.getTypesOnClasspath(getLookupCall().getJavaProject()), TypeFilters.getInterfaceFilter());
      for (IType candidate : serviceHierarchy.getAllSubtypes(iService, serviceFilter, null)) {
        if (candidate.getElementName().equals(serviceName)) {
          m_lookupServiceInterface = candidate;
          break;
        }
      }
    }
    if (m_lookupServiceInterface != null && m_lookupService == null) {
      String serviceName = getLookupCall().getElementName().replaceAll("^(.*)" + ScoutIdeProperties.SUFFIX_LOOKUP_CALL + "$", "$1" + ScoutIdeProperties.SUFFIX_LOOKUP_SERVICE);

      ITypeFilter serviceFilter = TypeFilters.getMultiTypeFilter(TypeFilters.getInScoutProject(ScoutSdk.getScoutWorkspace().getScoutBundle(getLookupCall().getJavaProject().getProject()).getScoutProject()), TypeFilters.getClassFilter());
      for (IType candidate : serviceHierarchy.getAllSubtypes(iService, serviceFilter, null)) {
        if (candidate.getElementName().equals(serviceName)) {
          m_lookupService = candidate;
          break;
        }
      }
    }
    if (m_lookupServiceInterface != null) {
      members.add(m_lookupServiceInterface);
    }
    if (m_lookupService != null) {
      members.add(m_lookupService);
    }

  }

  /**
   * @return the lookupCall
   */
  public IType getLookupCall() {
    return m_lookupCall;
  }

  private class P_SelectionValidationListener implements IMemberSelectionChangedListener {
    public void handleSelectionChanged(IMember[] selection) {
      m_confirmDialog.setMessage("");
      HashSet<IMember> members = new HashSet<IMember>(Arrays.asList(selection));
      boolean canOk = true;
      if (selection == null || selection.length == 0) {
        canOk = false;
      }
      if (m_lookupService != null && m_lookupServiceInterface != null && (members.contains(m_lookupServiceInterface) != members.contains(m_lookupService))) {
        m_confirmDialog.setMessage("Process service interface and implementation must have the same selection.", IMessageProvider.ERROR);
        canOk = false;
      }
      m_confirmDialog.getOkButton().setEnabled(canOk);

    }
  }
}
