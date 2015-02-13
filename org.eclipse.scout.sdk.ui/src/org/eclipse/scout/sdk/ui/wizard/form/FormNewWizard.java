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
package org.eclipse.scout.sdk.ui.wizard.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.operation.form.FormStackNewOperation;
import org.eclipse.scout.sdk.operation.service.ServiceRegistrationDescription;
import org.eclipse.scout.sdk.ui.fields.bundletree.DndEvent;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeDndListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.wizard.AbstractServiceWizard;
import org.eclipse.scout.sdk.ui.wizard.BundleTreeWizardPage;
import org.eclipse.scout.sdk.ui.wizard.IStatusProvider;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.dnd.DND;
import org.eclipse.ui.IWorkbench;

/**
 * <h3> {@link FormNewWizard}</h3> To create a new form.
 *
 * @author Andreas Hoegger
 * @since 1.0.8 05.11.2009
 */
public class FormNewWizard extends AbstractServiceWizard {
  public static final String TYPE_FORM = "form";
  public static final String TYPE_FORM_BUTTON_OK = "okbutton";
  public static final String TYPE_FORM_BUTTON_CANCEL = "cancelbutton";
  public static final String TYPE_HANDLER_MODIFY = "modifyHandler";
  public static final String TYPE_HANDLER_NEW = "newHandler";
  public static final String TYPE_FORM_DATA = "formData";
  public static final String TYPE_PERMISSION_CREATE = "createPerm";
  public static final String TYPE_PERMISSION_READ = "readPerm";
  public static final String TYPE_PERMISSION_UPDATE = "updatePerm";
  public static final String TYPE_SERVICE_INTERFACE = "svcIfc";
  public static final String TYPE_SERVICE_IMPLEMENTATION = "svcImpl";
  public static final String TYPE_SERVICE_REG_CLIENT = "svcRegClient";
  public static final String TYPE_SERVICE_REG_SERVER = "svcRegServer";

  private IScoutBundle m_clientBundle;
  private FormNewWizardPage m_formPage;
  private BundleTreeWizardPage m_locationPage;
  private ITreeNode m_locationPageRoot;
  private FormStackNewOperation m_operation;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    setWindowTitle(Texts.get("NewForm"));

    m_clientBundle = UiUtility.getScoutBundleFromSelection(selection, ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT));
    String pck = UiUtility.getPackageSuffix(selection);

    m_formPage = new FormNewWizardPage(m_clientBundle);
    m_formPage.addPropertyChangeListener(new P_LocationPropertyListener());
    m_formPage.setTargetPackage(pck);
    addPage(m_formPage);

    if (m_clientBundle != null) {
      m_locationPageRoot = createTree(m_clientBundle);
      m_locationPage = new BundleTreeWizardPage(Texts.get("FormClassLocations"), Texts.get("OrganiseLocations"), m_locationPageRoot, new P_InitialCheckedFilter());
      m_locationPage.addStatusProvider(new P_StatusRevalidator());
      m_locationPage.addDndListener(new P_TreeDndListener());
      m_locationPage.addCheckSelectionListener(new P_SessionCheckListener());
      addPage(m_locationPage);
    }
  }

  private ITreeNode createTree(IScoutBundle clientBundle) {
    IScoutBundle serverBundle = null;
    IScoutBundle sharedBundle = null;
    if (clientBundle == null) {
      return null;
    }
    sharedBundle = clientBundle.getParentBundle(ScoutBundleFilters.getMultiFilterAnd(ScoutBundleFilters.getWorkspaceBundlesFilter(), ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED)), false);
    if (sharedBundle != null) {
      serverBundle = sharedBundle.getChildBundle(ScoutBundleFilters.getMultiFilterAnd(ScoutBundleFilters.getWorkspaceBundlesFilter(), ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SERVER)), clientBundle, false);
    }

    IScoutBundleFilter bundleFilter = ScoutBundleFilters.getMultiFilterAnd(ScoutBundleFilters.getWorkspaceBundlesFilter(), ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT, IScoutBundle.TYPE_SERVER, IScoutBundle.TYPE_SHARED));
    ITreeNode rootNode = TreeUtility.createBundleTree(clientBundle, bundleFilter);

    ITreeNode clientNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(getClientBundle()));
    // form
    ITreeNode formNode = TreeUtility.createNode(clientNode, TYPE_FORM, Texts.get("Form"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), 1);
    formNode.setEnabled(false);
    // form buttons
    TreeUtility.createNode(formNode, TYPE_FORM_BUTTON_OK, Texts.get("OkButton"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), 1);
    TreeUtility.createNode(formNode, TYPE_FORM_BUTTON_CANCEL, Texts.get("CancelButton"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), 2);

    // newHandler
    TreeUtility.createNode(formNode, TYPE_HANDLER_NEW, Texts.get("NewHandler"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), 3);
    // modifyHandler
    TreeUtility.createNode(formNode, TYPE_HANDLER_MODIFY, Texts.get("ModifyHandler"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), 4);
    if (sharedBundle != null && serverBundle != null) {
      // service client reg
      TreeUtility.createNode(clientNode, TYPE_SERVICE_REG_CLIENT, Texts.get("ServiceProxyRegistration"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Public), 2);
    }

    if (sharedBundle != null) {
      ITreeNode sharedNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(sharedBundle));
      // formData
      TreeUtility.createNode(sharedNode, TYPE_FORM_DATA, Texts.get("FormData"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), 1);
      // service interface
      if (serverBundle != null) {
        TreeUtility.createNode(sharedNode, TYPE_SERVICE_INTERFACE, Texts.get("IService"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Interface), 2);
      }
      // permission create
      TreeUtility.createNode(sharedNode, TYPE_PERMISSION_CREATE, Texts.get("CreatePermission"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), 3);
      // permission read
      TreeUtility.createNode(sharedNode, TYPE_PERMISSION_READ, Texts.get("ReadPermission"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), 4);
      // permission update
      TreeUtility.createNode(sharedNode, TYPE_PERMISSION_UPDATE, Texts.get("UpdatePermission"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), 5);
    }
    if (serverBundle != null) {
      ITreeNode serverNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(serverBundle));
      // service implementation
      TreeUtility.createNode(serverNode, TYPE_SERVICE_IMPLEMENTATION, Texts.get("Service"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), 1);
      // service implementation
      ITreeNode svcRegNode = TreeUtility.createNode(serverNode, TYPE_SERVICE_REG_SERVER, Texts.get("ServiceRegistration"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Public), 2);

      // server sessions
      refreshAvailableSessions(svcRegNode, svcRegNode);
    }
    return rootNode;
  }

  public FormNewWizardPage getFormNewPage() {
    return m_formPage;
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    // fill operation before gui is disposed
    IScoutBundle formBundle = m_locationPage.getLocationBundle(TYPE_FORM, true, true);
    m_operation = new FormStackNewOperation(m_formPage.getTypeName(), formBundle.getPackageName(m_formPage.getTargetPackage(IDefaultTargetPackage.CLIENT_FORMS)), formBundle.getJavaProject());
    m_formPage.fillOperation(m_operation);
    m_operation.setCreateButtonOk(m_locationPage.getTreeNode(TYPE_FORM_BUTTON_OK, true, true) != null);
    m_operation.setCreateButtonCancel(m_locationPage.getTreeNode(TYPE_FORM_BUTTON_CANCEL, true, true) != null);
    m_operation.setCreateModifyHandler(m_locationPage.getTreeNode(TYPE_HANDLER_MODIFY, true, true) != null);
    m_operation.setCreateNewHandler(m_locationPage.getTreeNode(TYPE_HANDLER_NEW, true, true) != null);
    IScoutBundle formDataBundle = m_locationPage.getLocationBundle(TYPE_FORM_DATA, true, true);
    if (formDataBundle != null) {
      m_operation.setFormDataProject(formDataBundle.getJavaProject());
      m_operation.setFormDataPackage(formDataBundle.getPackageName(m_formPage.getTargetPackage(IDefaultTargetPackage.SHARED_SERVICES)));
    }
    Set<IScoutBundle> proxyRegistrationBundles = m_locationPage.getLocationBundles(TYPE_SERVICE_REG_CLIENT, true, true);
    List<IJavaProject> proxyRegistrationProjects = new ArrayList<IJavaProject>(proxyRegistrationBundles.size());
    for (IScoutBundle b : proxyRegistrationBundles) {
      proxyRegistrationProjects.add(ScoutUtility.getJavaProject(b));
    }
    m_operation.setServiceProxyRegistrationProjects(proxyRegistrationProjects);

    IScoutBundle permCreateBundle = m_locationPage.getLocationBundle(TYPE_PERMISSION_CREATE, true, true);
    if (permCreateBundle != null) {
      m_operation.setPermissionCreateProject(permCreateBundle.getJavaProject());
      m_operation.setPermissionCreateName(m_locationPage.getTextOfNode(TYPE_PERMISSION_CREATE, true, true));
      m_operation.setPermissionCreatePackage(permCreateBundle.getPackageName(m_formPage.getTargetPackage(IDefaultTargetPackage.SHARED_SECURITY)));
    }

    IScoutBundle permReadBundle = m_locationPage.getLocationBundle(TYPE_PERMISSION_READ, true, true);
    if (permReadBundle != null) {
      m_operation.setPermissionReadProject(permReadBundle.getJavaProject());
      m_operation.setPermissionReadName(m_locationPage.getTextOfNode(TYPE_PERMISSION_READ, true, true));
      m_operation.setPermissionReadPackage(permReadBundle.getPackageName(m_formPage.getTargetPackage(IDefaultTargetPackage.SHARED_SECURITY)));
    }

    IScoutBundle permUpdateBundle = m_locationPage.getLocationBundle(TYPE_PERMISSION_UPDATE, true, true);
    if (permUpdateBundle != null) {
      m_operation.setPermissionUpdateProject(permUpdateBundle.getJavaProject());
      m_operation.setPermissionUpdateName(m_locationPage.getTextOfNode(TYPE_PERMISSION_UPDATE, true, true));
      m_operation.setPermissionUpdatePackage(permUpdateBundle.getPackageName(m_formPage.getTargetPackage(IDefaultTargetPackage.SHARED_SECURITY)));
    }

    for (ServiceRegistrationDescription desc : getCheckedServiceRegistrations(m_locationPage.getTreeNodes(TYPE_SERVICE_REG_SERVER, true, true))) {
      m_operation.addServiceRegistration(desc);
      storeUsedSession(desc);
    }

    IScoutBundle serviceImplBundle = m_locationPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
    if (serviceImplBundle != null) {
      m_operation.setServiceImplementationProject(serviceImplBundle.getJavaProject());
      m_operation.setServiceImplementationName(m_locationPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION, true, true));
      m_operation.setServiceImplementationPackage(serviceImplBundle.getPackageName(m_formPage.getTargetPackage(IDefaultTargetPackage.SERVER_SERVICES)));
    }

    IScoutBundle serviceInterfaceBundle = m_locationPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
    if (serviceInterfaceBundle != null) {
      m_operation.setServiceInterfaceProject(serviceInterfaceBundle.getJavaProject());
      m_operation.setServiceInterfaceName(m_locationPage.getTextOfNode(TYPE_SERVICE_INTERFACE, true, true));
      m_operation.setServiceInterfacePackage(serviceInterfaceBundle.getPackageName(m_formPage.getTargetPackage(IDefaultTargetPackage.SHARED_SERVICES)));
    }
    return true;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    try {
      m_operation.run(monitor, workingCopyManager);
      return true;
    }
    catch (Exception e) {
      ScoutSdkUi.logError("exception during perfoming finish of wizard.", e);
      return false;
    }
  }

  @Override
  public BundleTreeWizardPage getLocationsPage() {
    return m_locationPage;
  }

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }

  private class P_LocationPropertyListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals(FormNewWizardPage.PROP_TYPE_NAME)) {
        String typeName = m_formPage.getTypeName();
        if (!StringUtility.isNullOrEmpty(typeName)) {
          String prefix = typeName.replaceAll(SdkProperties.SUFFIX_FORM + "$", "");
          TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_FORM)).setText(prefix + SdkProperties.SUFFIX_FORM);
          ITreeNode formDataNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_FORM_DATA));
          if (formDataNode != null) {
            formDataNode.setText(prefix + SdkProperties.SUFFIX_FORM_DATA);
          }
          ITreeNode permissionCreateNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_PERMISSION_CREATE));
          if (permissionCreateNode != null) {
            permissionCreateNode.setText("Create" + prefix + SdkProperties.SUFFIX_PERMISSION);
          }
          ITreeNode permissionReadNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_PERMISSION_READ));
          if (permissionReadNode != null) {
            permissionReadNode.setText("Read" + prefix + SdkProperties.SUFFIX_PERMISSION);
          }

          ITreeNode permissionUpdateNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_PERMISSION_UPDATE));
          if (permissionUpdateNode != null) {
            permissionUpdateNode.setText("Update" + prefix + SdkProperties.SUFFIX_PERMISSION);
          }

          String legacyServiceAddName = null;
          if (DefaultTargetPackage.isPackageConfigurationEnabled()) {
            legacyServiceAddName = "";
          }
          else {
            legacyServiceAddName = "Process";
          }
          ITreeNode serviceImplNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SERVICE_IMPLEMENTATION));
          if (serviceImplNode != null) {
            serviceImplNode.setText(prefix + legacyServiceAddName + SdkProperties.SUFFIX_SERVICE);
          }
          ITreeNode serviceInterfaceNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SERVICE_INTERFACE));
          if (serviceInterfaceNode != null) {
            serviceInterfaceNode.setText("I" + prefix + legacyServiceAddName + SdkProperties.SUFFIX_SERVICE);
          }
          m_locationPage.refreshTree();
        }
        m_locationPage.pingStateChanging();
      }
    }
  } // end class P_LocationPropertyListener

  private class P_TreeDndListener implements ITreeDndListener {
    @Override
    public boolean isDragableNode(ITreeNode node) {
      return TreeUtility.isOneOf(node.getType(), TYPE_FORM, TYPE_PERMISSION_CREATE, TYPE_PERMISSION_READ,
          TYPE_PERMISSION_UPDATE, TYPE_SERVICE_IMPLEMENTATION, TYPE_SERVICE_INTERFACE,
          TYPE_SERVICE_REG_CLIENT, TYPE_SERVICE_REG_SERVER);
    }

    @Override
    public void validateTarget(DndEvent dndEvent) {
      if (dndEvent.targetParent == null) {
        dndEvent.doit = false;
        return;
      }
      if (dndEvent.targetParent.getData() instanceof IScoutBundle && ((IScoutBundle) dndEvent.targetParent.getData()).isBinary()) {
        dndEvent.doit = false;
        return;
      }
      if (dndEvent.operation == DND.DROP_COPY) {
        validateDropCopy(dndEvent);
      }
      else if (dndEvent.operation == DND.DROP_MOVE) {
        validateDropMove(dndEvent);
      }
    }

    @Override
    public void dndPerformed(DndEvent dndEvent) {
      if (TYPE_FORM.equals(dndEvent.node.getType())) {
        ITreeNode formDataNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_FORM_DATA));
        if (formDataNode != null) {
          ITreeNode oldFomDataParent = formDataNode.getParent();
          IScoutBundle formBundle = (IScoutBundle) dndEvent.node.getParent().getData();
          Set<? extends IScoutBundle> sharedBundles = formBundle.getParentBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED), false);
          for (IScoutBundle formDataBundle : sharedBundles) {
            ITreeNode sharedNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByData(formDataBundle));
            if (sharedNode != null) {
              if (!oldFomDataParent.equals(sharedNode)) {
                // move
                oldFomDataParent.removeChild(formDataNode);
                formDataNode.setParent(sharedNode);
                sharedNode.addChild(formDataNode);
              }
              break;
            }
          }
        }
      }
      else if (TYPE_SERVICE_REG_SERVER.equals(dndEvent.node.getType())) {
        refreshAvailableSessions(dndEvent.newNode, dndEvent.node);
      }
      m_formPage.pingStateChanging();
    }

    private void validateDropCopy(DndEvent dndEvent) {
      if (TYPE_SERVICE_REG_CLIENT.equals(dndEvent.node.getType())) {
        dndEvent.doit = IScoutBundle.TYPE_CLIENT.equals(dndEvent.targetParent.getType());
      }
      else if (TYPE_SERVICE_REG_SERVER.equals(dndEvent.node.getType())) {
        dndEvent.doit = IScoutBundle.TYPE_SERVER.equals(dndEvent.targetParent.getType());
      }
      else {
        dndEvent.doit = false;
      }
    }

    private void validateDropMove(DndEvent dndEvent) {
      String type = dndEvent.node.getType();
      if (TreeUtility.isOneOf(type, TYPE_FORM, TYPE_SERVICE_REG_CLIENT)) {
        dndEvent.doit = IScoutBundle.TYPE_CLIENT.equals(dndEvent.targetParent.getType());
      }
      else if (TreeUtility.isOneOf(type, TYPE_PERMISSION_CREATE, TYPE_PERMISSION_READ, TYPE_PERMISSION_UPDATE, TYPE_SERVICE_INTERFACE)) {
        dndEvent.doit = IScoutBundle.TYPE_SHARED.equals(dndEvent.targetParent.getType());
      }
      else if (TreeUtility.isOneOf(type, TYPE_SERVICE_IMPLEMENTATION, TYPE_SERVICE_REG_SERVER)) {
        dndEvent.doit = IScoutBundle.TYPE_SERVER.equals(dndEvent.targetParent.getType());
      }
      else {
        dndEvent.doit = false;
      }
    }
  } // end class P_TreeDndListener

  private class P_StatusRevalidator implements IStatusProvider {

    @Override
    public void validate(Object source, MultiStatus multiStatus) {
      multiStatus.add(getStatusForm());
      multiStatus.add(getStatusService());
      multiStatus.add(getStatusServiceRegistrationClient());
      multiStatus.add(getStatusServiceRegistrationServer());
      multiStatus.add(getStatusTypeNames());
    }

    protected IStatus getStatusTypeNames() {
      IScoutBundle serviceImplementationBundle = m_locationPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
      if (serviceImplementationBundle != null) {
        ITreeNode serviceImplNode = m_locationPage.getTreeNode(TYPE_SERVICE_IMPLEMENTATION, true, true);
        if (serviceImplNode != null) {
          try {
            String fqn = serviceImplementationBundle.getPackageName(m_formPage.getTargetPackage(IDefaultTargetPackage.SERVER_SERVICES)) + "." + serviceImplNode.getText();
            IType findType = serviceImplementationBundle.getJavaProject().findType(fqn);
            if (TypeUtility.exists(findType)) {
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + serviceImplNode.getText() + "' " + Texts.get("AlreadyExists") + ".");
            }
          }
          catch (JavaModelException e) {
            ScoutSdkUi.logError(e);
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("AnErrorOccured"));
          }
        }
      }
      // shared bundles
      IScoutBundle serviceInterfaceBundle = m_locationPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
      if (serviceInterfaceBundle != null) {
        ITreeNode serviceInterfaceNode = m_locationPage.getTreeNode(TYPE_SERVICE_INTERFACE, true, true);
        if (serviceInterfaceNode != null) {
          try {
            String fqn = serviceInterfaceBundle.getPackageName(m_formPage.getTargetPackage(IDefaultTargetPackage.SHARED_SERVICES)) + "." + serviceInterfaceNode.getText();
            IType interfaceType = serviceInterfaceBundle.getJavaProject().findType(fqn);
            if (TypeUtility.exists(interfaceType)) {
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + serviceInterfaceNode.getText() + "' " + Texts.get("AlreadyExists") + ".");
            }
          }
          catch (JavaModelException e) {
            ScoutSdkUi.logError(e);
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("AnErrorOccured"));
          }
        }
      }
      // permission read
      IStatus createStatus = getStatusPermission(TYPE_PERMISSION_CREATE);
      if (createStatus != Status.OK_STATUS) {
        return createStatus;
      }
      IStatus readStatus = getStatusPermission(TYPE_PERMISSION_READ);
      if (readStatus != Status.OK_STATUS) {
        return readStatus;
      }
      IStatus updateStatus = getStatusPermission(TYPE_PERMISSION_UPDATE);
      if (updateStatus != Status.OK_STATUS) {
        return updateStatus;
      }
      return Status.OK_STATUS;
    }

    protected IStatus getStatusPermission(String permissionType) {
      IScoutBundle permissionBundle = m_locationPage.getLocationBundle(permissionType, true, true);
      if (permissionBundle != null) {
        ITreeNode permissionNode = m_locationPage.getTreeNode(permissionType, true, true);
        if (permissionNode != null) {
          try {
            String fqn = permissionBundle.getPackageName(m_formPage.getTargetPackage(IDefaultTargetPackage.SHARED_SECURITY)) + "." + permissionNode.getText();
            IType permission = permissionBundle.getJavaProject().findType(fqn);
            if (permission != null && permission.exists()) {
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + permissionNode.getText() + "' " + Texts.get("AlreadyExists") + ".");
            }
          }
          catch (JavaModelException e) {
            ScoutSdkUi.logError(e);
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("AnErrorOccured"));
          }
        }
      }
      return Status.OK_STATUS;
    }

    protected IStatus getStatusForm() {
      IScoutBundle formBundle = m_locationPage.getLocationBundle(TYPE_FORM, true, true);
      if (formBundle != null) {
        IScoutBundle formDataBundle = m_locationPage.getLocationBundle(TYPE_FORM_DATA, true, true);
        if (formDataBundle != null) {
          if (!ScoutTypeUtility.isOnClasspath(formDataBundle, formBundle)) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_locationPage.getTextOfNode(TYPE_FORM_DATA), m_locationPage.getTextOfNode(TYPE_FORM)));
          }
        }
        IScoutBundle formServiceBundle = m_locationPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
        if (formServiceBundle != null) {
          if (!ScoutTypeUtility.isOnClasspath(formServiceBundle, formBundle)) {
            return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_locationPage.getTextOfNode(TYPE_SERVICE_INTERFACE), m_locationPage.getTextOfNode(TYPE_FORM)));
          }
        }
        IScoutBundle permissionCreateBundle = m_locationPage.getLocationBundle(TYPE_PERMISSION_CREATE, true, true);
        if (permissionCreateBundle != null) {
          if (!ScoutTypeUtility.isOnClasspath(permissionCreateBundle, formBundle)) {
            return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_locationPage.getTextOfNode(TYPE_PERMISSION_CREATE), m_locationPage.getTextOfNode(TYPE_FORM)));
          }
        }
        IScoutBundle permissionReadBundle = m_locationPage.getLocationBundle(TYPE_PERMISSION_READ, true, true);
        if (permissionReadBundle != null) {
          if (!ScoutTypeUtility.isOnClasspath(permissionReadBundle, formBundle)) {
            return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_locationPage.getTextOfNode(TYPE_PERMISSION_READ), m_locationPage.getTextOfNode(TYPE_FORM)));
          }
        }
        IScoutBundle permissionUpdateBundle = m_locationPage.getLocationBundle(TYPE_PERMISSION_UPDATE, true, true);
        if (permissionUpdateBundle != null) {
          if (!ScoutTypeUtility.isOnClasspath(permissionUpdateBundle, formBundle)) {
            return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_locationPage.getTextOfNode(TYPE_PERMISSION_UPDATE), m_locationPage.getTextOfNode(TYPE_FORM)));
          }
        }
      }
      return Status.OK_STATUS;
    }

    protected IStatus getStatusService() {
      IScoutBundle serviceImplementationBundle = m_locationPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
      if (serviceImplementationBundle != null) {
        IScoutBundle serviceInterfaceBundle = m_locationPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
        if (serviceInterfaceBundle != null) {
          if (!ScoutTypeUtility.isOnClasspath(serviceInterfaceBundle, serviceImplementationBundle)) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_locationPage.getTextOfNode(TYPE_SERVICE_INTERFACE), m_locationPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION)));
          }
        }
        IScoutBundle permissionCreateBundle = m_locationPage.getLocationBundle(TYPE_PERMISSION_CREATE, true, true);
        if (permissionCreateBundle != null) {
          if (!ScoutTypeUtility.isOnClasspath(permissionCreateBundle, serviceImplementationBundle)) {
            return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_locationPage.getTextOfNode(TYPE_PERMISSION_CREATE), m_locationPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION)));
          }
        }
        IScoutBundle permissionReadBundle = m_locationPage.getLocationBundle(TYPE_PERMISSION_READ, true, true);
        if (permissionReadBundle != null) {
          if (!ScoutTypeUtility.isOnClasspath(permissionReadBundle, serviceImplementationBundle)) {
            return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_locationPage.getTextOfNode(TYPE_PERMISSION_READ), m_locationPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION)));
          }
        }
        IScoutBundle permissionUpdateBundle = m_locationPage.getLocationBundle(TYPE_PERMISSION_UPDATE, true, true);
        if (permissionUpdateBundle != null) {
          if (!ScoutTypeUtility.isOnClasspath(permissionUpdateBundle, serviceImplementationBundle)) {
            return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_locationPage.getTextOfNode(TYPE_PERMISSION_UPDATE), m_locationPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION)));
          }
        }
      }
      return Status.OK_STATUS;
    }

    protected IStatus getStatusServiceRegistrationClient() {
      IScoutBundle serviceInterfaceBundle = m_locationPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
      Set<ITreeNode> serviceRegistrationClientNodes = m_locationPage.getTreeNodes(TYPE_SERVICE_REG_CLIENT, true, true);
      for (ITreeNode serviceRegNode : serviceRegistrationClientNodes) {
        Object data = serviceRegNode.getParent().getData();
        if (data instanceof IScoutBundle) {
          IScoutBundle serviceRegistrationBundle = (IScoutBundle) data;
          if (serviceInterfaceBundle == null) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("NoClientSvcRegWithoutASvcInterf"));
          }
          else if (!ScoutTypeUtility.isOnClasspath(serviceInterfaceBundle, serviceRegistrationBundle)) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotOnClasspathOfServiceY", m_locationPage.getTextOfNode(TYPE_SERVICE_INTERFACE), serviceRegistrationBundle.getSymbolicName()));
          }
        }
      }
      return Status.OK_STATUS;
    }

    protected IStatus getStatusServiceRegistrationServer() {
      IScoutBundle serviceImplementationBundle = m_locationPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
      Set<ITreeNode> serviceRegistrationServerNodes = m_locationPage.getTreeNodes(TYPE_SERVICE_REG_SERVER, true, true);
      for (ITreeNode serviceRegNode : serviceRegistrationServerNodes) {
        IScoutBundle serviceRegistrationBundle = (IScoutBundle) serviceRegNode.getParent().getData();
        if (serviceImplementationBundle != null) {
          if (!ScoutTypeUtility.isOnClasspath(serviceImplementationBundle, serviceRegistrationBundle)) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotOnClasspathOfServiceY", m_locationPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION), serviceRegistrationBundle.getSymbolicName()));
          }
        }
      }
      return Status.OK_STATUS;
    }
  } // end class P_StatusRevalidator
}
