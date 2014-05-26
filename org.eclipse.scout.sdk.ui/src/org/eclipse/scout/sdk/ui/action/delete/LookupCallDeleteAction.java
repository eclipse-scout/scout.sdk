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
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.jdt.JavaElementDeleteOperation;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.dialog.IMemberSelectionChangedListener;
import org.eclipse.scout.sdk.ui.dialog.MemberSelectionDialog;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
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
 * <h3>LookupCallDeleteAction</h3> The ui action to delete a lookup call. If a process service has the same name pattern
 * (e.g. CompanyLookupCall -> (I)CompanyLookupService the user will be asked to delete
 * also the service.
 */
public class LookupCallDeleteAction extends AbstractScoutHandler {

  private IType m_lookupCall;
  // action members
  private IType m_lookupService;
  private IType m_lookupServiceInterface;
  private MemberSelectionDialog m_confirmDialog;

  public LookupCallDeleteAction() {
    super(Texts.get("DeleteLookupCall") + "...", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.LookupCallRemove), "Delete", false, Category.DELETE);
  }

  @Override
  public boolean isVisible() {
    return isEditable(m_lookupCall);
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    m_confirmDialog = new MemberSelectionDialog(shell, Texts.get("DeleteLookupCall"));
    m_confirmDialog.addMemberSelectionListener(new P_SelectionValidationListener());
    List<IMember> members = new ArrayList<IMember>();
    List<IMember> selectedMembers = new ArrayList<IMember>();
    collectAffectedMembers(members, selectedMembers);
    m_confirmDialog.setMembers(members.toArray(new IMember[members.size()]));
    m_confirmDialog.setSelectedMembers(selectedMembers.toArray(new IMember[selectedMembers.size()]));
    if (m_confirmDialog.open() == Dialog.OK) {
      JavaElementDeleteOperation op = new JavaElementDeleteOperation() {
        @Override
        protected void deleteMember(IJavaElement member, Set<ICompilationUnit> icuForOrganizeImports, IProgressMonitor monitor, IWorkingCopyManager manager) throws CoreException {
          if (m_lookupServiceInterface != null && member.equals(m_lookupServiceInterface)) {
            ScoutUtility.unregisterServiceProxy(m_lookupServiceInterface);
          }
          if (m_lookupService != null && member.equals(m_lookupService)) {
            ScoutUtility.unregisterServiceImplementation(m_lookupService);
          }
          super.deleteMember(member, icuForOrganizeImports, monitor, manager);
        }
      };
      op.setMembers(m_confirmDialog.getSelectedMembers());

      OperationJob job = new OperationJob(op);
      job.schedule();
    }
    return null;
  }

  protected void collectAffectedMembers(List<IMember> members, List<IMember> selectedMembers) {
    members.add(getLookupCall());
    selectedMembers.add(getLookupCall());
    // find lookup service
    IType iService = TypeUtility.getType(IRuntimeClasses.ILookupService);
    ICachedTypeHierarchy serviceHierarchy = TypeUtility.getPrimaryTypeHierarchy(iService);
    if (m_lookupServiceInterface == null) {
      String serviceName = getLookupCall().getElementName().replaceAll("^(.*)" + SdkProperties.SUFFIX_LOOKUP_CALL + "$", "I$1" + SdkProperties.SUFFIX_LOOKUP_SERVICE);
      ITypeFilter serviceFilter = TypeFilters.getMultiTypeFilter(TypeFilters.getTypesOnClasspath(getLookupCall().getJavaProject()), TypeFilters.getInterfaceFilter());
      for (IType candidate : serviceHierarchy.getAllSubtypes(iService, serviceFilter, null)) {
        if (candidate.getElementName().equals(serviceName)) {
          m_lookupServiceInterface = candidate;
          break;
        }
      }
    }
    if (m_lookupServiceInterface != null && m_lookupService == null) {
      String serviceName = getLookupCall().getElementName().replaceAll("^(.*)" + SdkProperties.SUFFIX_LOOKUP_CALL + "$", "$1" + SdkProperties.SUFFIX_LOOKUP_SERVICE);
      IScoutBundle sharedBundle = ScoutTypeUtility.getScoutBundle(getLookupCall());
      Set<IScoutBundle> visibleServers = sharedBundle.getChildBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SERVER), false);
      ITypeFilter serviceFilter = TypeFilters.getMultiTypeFilter(ScoutTypeFilters.getInScoutBundles(visibleServers), TypeFilters.getClassFilter());
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

  public void setLookupCall(IType lookupCall) {
    m_lookupCall = lookupCall;
  }

  private class P_SelectionValidationListener implements IMemberSelectionChangedListener {
    @Override
    public void handleSelectionChanged(IMember[] selection) {
      m_confirmDialog.setMessage("");
      boolean canOk = true;
      if (selection == null || selection.length == 0) {
        canOk = false;
      }
      else {
        HashSet<IMember> members = new HashSet<IMember>(Arrays.asList(selection));
        if (m_lookupService != null && m_lookupServiceInterface != null && (members.contains(m_lookupServiceInterface) != members.contains(m_lookupService))) {
          m_confirmDialog.setMessage(Texts.get("ProcessServiceSelection"), IMessageProvider.ERROR);
          canOk = false;
        }
      }
      m_confirmDialog.getOkButton().setEnabled(canOk);
    }
  }
}
