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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.scout.commons.CollectionUtility;
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
import org.eclipse.scout.sdk.util.type.TypeComparators;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>FormDeleteAction</h3> The ui action to delete a form. If a process service has the same name pattern
 * (e.g. CompanyForm -> (I)CompanyProcessService the user will be asked to delete
 * also the service.
 */
public class FormDeleteAction extends AbstractScoutHandler {

  private IType m_formType;
  private IType m_formData;
  private Set<IType> m_processServiceInterface;
  private Set<IType> m_processServiceImplementation;
  private MemberSelectionDialog m_confirmDialog;

  public FormDeleteAction() {
    super(Texts.get("DeleteForm"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.FormRemove), "Delete", false, Category.DELETE);
    m_processServiceImplementation = new HashSet<IType>();
  }

  @Override
  public boolean isVisible() {
    return isEditable(m_formType);
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    m_confirmDialog = new MemberSelectionDialog(shell, Texts.get("Action_deleteTypeX", getFormType().getElementName()));
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
          if (m_processServiceInterface.contains(member)) {
            ScoutUtility.unregisterServiceProxy((IType) member);
          }
          if (m_processServiceImplementation.contains(member)) {
            ScoutUtility.unregisterServiceImplementation((IType) member);
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
    // form
    members.add(m_formType);
    selectedMembers.add(m_formType);

    // form data
    if (m_formData == null) {
      try {
        m_formData = ScoutTypeUtility.findFormDataForForm(getFormType());
      }
      catch (JavaModelException e) {
        ScoutSdkUi.logError(e);
      }
    }
    if (TypeUtility.exists(m_formData)) {
      members.add(m_formData);
      selectedMembers.add(m_formData);
    }

    // find process service interfaces
    final String formName = getFormType().getElementName().replaceAll("^(.*)" + SdkProperties.SUFFIX_FORM + "$", "$1");
    IType iService = TypeUtility.getType(IRuntimeClasses.IService);
    m_processServiceInterface = TypeUtility.getInterfacesOnClasspath(iService, getFormType().getJavaProject(),
        TypeFilters.getRegexSimpleNameFilter("I" + formName + "(Process)?" + SdkProperties.SUFFIX_SERVICE, 0));
    members.addAll(m_processServiceInterface);

    // process service implementations
    for (IType serviceInterface : m_processServiceInterface) {
      IScoutBundle sharedBundle = ScoutTypeUtility.getScoutBundle(serviceInterface);
      Set<IScoutBundle> visibleServers = sharedBundle.getChildBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SERVER), false);
      ITypeFilter serviceFilter = TypeFilters.getMultiTypeFilterAnd(ScoutTypeFilters.getInScoutBundles(visibleServers), TypeFilters.getClassFilter());
      Set<IType> implementations = TypeUtility.getPrimaryTypeHierarchy(serviceInterface).getAllSubtypes(serviceInterface, serviceFilter, TypeComparators.getTypeNameComparator());
      m_processServiceImplementation.addAll(implementations);
    }
    members.addAll(m_processServiceImplementation);

    // find Permissions
    String permissionRegex = getFormType().getElementName().replaceAll("^(.*)" + SdkProperties.SUFFIX_FORM + "$", "(Create|Read|Update)$1" + SdkProperties.SUFFIX_PERMISSION);
    IType permission = TypeUtility.getType(Permission.class.getName());
    Set<IType> permissions = TypeUtility.getClassesOnClasspath(permission, getFormType().getJavaProject(), TypeFilters.getRegexSimpleNameFilter(permissionRegex, 0));
    members.addAll(permissions);
  }

  public IType getFormType() {
    return m_formType;
  }

  public void setFormType(IType formType) {
    m_formType = formType;
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
        Set<IMember> members = CollectionUtility.hashSet(selection);
        if (m_formType != null && m_formData != null && (members.contains(m_formType) != members.contains(m_formData))) {
          m_confirmDialog.setMessage(Texts.get("FormDataDeleteWithForm", m_formData.getElementName()), IMessageProvider.WARNING);
        }
      }
      m_confirmDialog.getOkButton().setEnabled(canOk);
    }
  }
}
