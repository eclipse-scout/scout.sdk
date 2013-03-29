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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.form.FormStackNewOperation;
import org.eclipse.scout.sdk.ui.fields.bundletree.DndEvent;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeDndListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNodeFilter;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.ui.wizard.BundleTreeWizardPage;
import org.eclipse.scout.sdk.ui.wizard.IStatusProvider;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.dnd.DND;

/**
 * <h3> {@link FormNewWizard}</h3> To create a new form.
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 05.11.2009
 */
public class FormNewWizard extends AbstractWorkspaceWizard {

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

  private final IScoutBundle m_clientBundle;
  private FormNewWizardPage m_formPage;
  private BundleTreeWizardPage m_locationPage;
  private FormStackNewOperation m_operation = new FormStackNewOperation(true);
  private ITreeNode m_locationPageRoot;

  public FormNewWizard(IScoutBundle clientBundle) {
    setWindowTitle(Texts.get("NewForm"));
    P_StatusRevalidator statusProvider = new P_StatusRevalidator();
    m_clientBundle = clientBundle;
    m_formPage = new FormNewWizardPage(getClientBundle());
    addPage(m_formPage);
    m_locationPageRoot = createTree(clientBundle);
    m_locationPage = new BundleTreeWizardPage(Texts.get("FormClassLocations"), Texts.get("OrganiseLocations"), m_locationPageRoot, new P_InitialCheckedFilter());
    m_locationPage.addStatusProvider(statusProvider);
    m_locationPage.addDndListener(new P_TreeDndListener());
    addPage(m_locationPage);
    // init
    m_formPage.addPropertyChangeListener(new P_LocationPropertyListener());
  }

  private ITreeNode createTree(IScoutBundle clientBundle) {
    IScoutBundle serverBundle = null;
    IScoutBundle sharedBundle = null;
    if (clientBundle != null) {
      sharedBundle = clientBundle.getParentBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED), false);
      if (sharedBundle != null) {
        serverBundle = sharedBundle.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SERVER), clientBundle, false);
      }
    }

    ITreeNode rootNode = TreeUtility.createBundleTree(clientBundle, NodeFilters.getByType(IScoutBundle.TYPE_CLIENT, IScoutBundle.TYPE_SERVER, IScoutBundle.TYPE_SHARED));

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
      TreeUtility.createNode(serverNode, TYPE_SERVICE_REG_SERVER, Texts.get("ServiceRegistration"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Public), 2);
    }
    return rootNode;
  }

  public FormNewWizardPage getFormNewPage() {
    return m_formPage;
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    // fill operation before gui is disposed
    m_formPage.fillOperation(m_operation);
    m_operation.setCreateButtonOk(m_locationPage.getTreeNode(TYPE_FORM_BUTTON_OK, true, true) != null);
    m_operation.setCreateButtonCancel(m_locationPage.getTreeNode(TYPE_FORM_BUTTON_CANCEL, true, true) != null);
    m_operation.setCreateModifyHandler(m_locationPage.getTreeNode(TYPE_HANDLER_MODIFY, true, true) != null);
    m_operation.setCreateNewHandler(m_locationPage.getTreeNode(TYPE_HANDLER_NEW, true, true) != null);
    IScoutBundle formBundle = m_locationPage.getLocationBundle(TYPE_FORM, true, true);
    if (formBundle != null) {
      m_operation.setFormBundle(formBundle);
      m_operation.setFormPackage(formBundle.getPackageName(m_formPage.getTargetPackage()));
    }
    IScoutBundle formDataBundle = m_locationPage.getLocationBundle(TYPE_FORM_DATA, true, true);
    if (formDataBundle != null) {
      m_operation.setFormDataBundle(formDataBundle);
      m_operation.setFormDataPackage(formDataBundle.getPackageName(m_formPage.getTargetPackage()));
    }

    m_operation.setClientServiceRegistryBundles(m_locationPage.getLocationBundles(TYPE_SERVICE_REG_CLIENT, true, true));

    IScoutBundle permCreateBundle = m_locationPage.getLocationBundle(TYPE_PERMISSION_CREATE, true, true);
    if (permCreateBundle != null) {
      m_operation.setPermissionCreateBundle(permCreateBundle);
      m_operation.setPermissionCreateName(m_locationPage.getTextOfNode(TYPE_PERMISSION_CREATE, true, true));
      m_operation.setPermissionCreatePackage(permCreateBundle.getPackageName(m_formPage.getTargetPackage()));
    }

    IScoutBundle permReadBundle = m_locationPage.getLocationBundle(TYPE_PERMISSION_READ, true, true);
    if (permReadBundle != null) {
      m_operation.setPermissionReadBundle(permReadBundle);
      m_operation.setPermissionReadName(m_locationPage.getTextOfNode(TYPE_PERMISSION_READ, true, true));
      m_operation.setPermissionReadPackage(permReadBundle.getPackageName(m_formPage.getTargetPackage()));
    }

    IScoutBundle permUpdateBundle = m_locationPage.getLocationBundle(TYPE_PERMISSION_UPDATE, true, true);
    if (permUpdateBundle != null) {
      m_operation.setPermissionUpdateBundle(permUpdateBundle);
      m_operation.setPermissionUpdateName(m_locationPage.getTextOfNode(TYPE_PERMISSION_UPDATE, true, true));
      m_operation.setPermissionUpdatePackage(permUpdateBundle.getPackageName(m_formPage.getTargetPackage()));
    }

    m_operation.setServerServiceRegistryBundles(m_locationPage.getLocationBundles(TYPE_SERVICE_REG_SERVER, true, true));

    IScoutBundle serviceImplBundle = m_locationPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
    if (serviceImplBundle != null) {
      m_operation.setServiceImplementationBundle(serviceImplBundle);
      m_operation.setServiceImplementationName(m_locationPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION, true, true));
      m_operation.setServiceImplementationPackage(serviceImplBundle.getPackageName(m_formPage.getTargetPackage()));
    }

    IScoutBundle serviceInterfaceBundle = m_locationPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
    if (serviceInterfaceBundle != null) {
      m_operation.setServiceInterfaceBundle(serviceInterfaceBundle);
      m_operation.setServiceInterfaceName(m_locationPage.getTextOfNode(TYPE_SERVICE_INTERFACE, true, true));
      m_operation.setServiceInterfacePackage(serviceInterfaceBundle.getPackageName(m_formPage.getTargetPackage()));
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

  public FormNewWizardPage getFormPage() {
    return m_formPage;
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
          ITreeNode serviceImplNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SERVICE_IMPLEMENTATION));
          if (serviceImplNode != null) {
            serviceImplNode.setText(prefix + SdkProperties.SUFFIX_SERVICE);
          }
          ITreeNode serviceInterfaceNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SERVICE_INTERFACE));
          if (serviceInterfaceNode != null) {
            serviceInterfaceNode.setText("I" + prefix + SdkProperties.SUFFIX_SERVICE);
          }
          m_locationPage.refreshTree();
        }
        m_locationPage.pingStateChanging();
      }
    }
  } // end class P_LocationPropertyListener

  private class P_InitialCheckedFilter implements ITreeNodeFilter {
    @Override
    public boolean accept(ITreeNode node) {
      return true;
    }
  } // end class P_InitialCheckedFilter

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
      if (dndEvent.node.getType() == TYPE_FORM) {
        ITreeNode formDataNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_FORM_DATA));
        if (formDataNode != null) {
          ITreeNode oldFomDataParent = formDataNode.getParent();
          IScoutBundle formBundle = (IScoutBundle) dndEvent.node.getParent().getData();
          IScoutBundle[] sharedBundles = formBundle.getParentBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED), false);
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
            String fqn = serviceImplementationBundle.getPackageName(m_formPage.getTargetPackage()) + "." + serviceImplNode.getText();
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
            String fqn = serviceInterfaceBundle.getPackageName(m_formPage.getTargetPackage()) + "." + serviceInterfaceNode.getText();
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
            String fqn = permissionBundle.getPackageName(m_formPage.getTargetPackage()) + "." + permissionNode.getText();
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
      ITreeNode[] serviceRegistrationClientNodes = m_locationPage.getTreeNodes(TYPE_SERVICE_REG_CLIENT, true, true);
      for (ITreeNode serviceRegNode : serviceRegistrationClientNodes) {
        Object data = serviceRegNode.getParent().getData();
        if (data instanceof IScoutBundle) {
          IScoutBundle serviceRegistrationBundle = (IScoutBundle) data;
          if (serviceInterfaceBundle == null && serviceRegistrationBundle != null) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "The client service registration can not be done without a service interface.");
          }
          if (serviceInterfaceBundle != null && serviceRegistrationBundle != null) {
            if (!ScoutTypeUtility.isOnClasspath(serviceInterfaceBundle, serviceRegistrationBundle)) {
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotOnClasspathOfServiceY", m_locationPage.getTextOfNode(TYPE_SERVICE_INTERFACE), serviceRegistrationBundle.getSymbolicName()));
            }
          }
        }
      }
      return Status.OK_STATUS;
    }

    protected IStatus getStatusServiceRegistrationServer() {
      IScoutBundle serviceImplementationBundle = m_locationPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
      ITreeNode[] serviceRegistrationServerNodes = m_locationPage.getTreeNodes(TYPE_SERVICE_REG_SERVER, true, true);
      for (ITreeNode serviceRegNode : serviceRegistrationServerNodes) {
        Object data = serviceRegNode.getParent().getData();
        if (data instanceof IScoutBundle) {
          IScoutBundle serviceRegistrationBundle = (IScoutBundle) data;
          if (serviceImplementationBundle != null && serviceRegistrationBundle != null) {
            if (!ScoutTypeUtility.isOnClasspath(serviceImplementationBundle, serviceRegistrationBundle)) {
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotOnClasspathOfServiceY", m_locationPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION), serviceRegistrationBundle.getSymbolicName()));
            }
          }
        }
      }
      return Status.OK_STATUS;
    }
  } // end class P_StatusRevalidator
}
