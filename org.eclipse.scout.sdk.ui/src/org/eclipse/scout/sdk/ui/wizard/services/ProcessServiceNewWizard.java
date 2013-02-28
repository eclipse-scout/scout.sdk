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
package org.eclipse.scout.sdk.ui.wizard.services;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.service.ProcessServiceNewOperation;
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
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.dnd.DND;

public class ProcessServiceNewWizard extends AbstractWorkspaceWizard {
  private static final String TYPE_PERMISSION_CREATE = "permCreate";
  private static final String TYPE_PERMISSION_READ = "permRead";
  private static final String TYPE_PERMISSION_UPDATE = "permUpdate";
  private static final String TYPE_SERVICE_INTERFACE = "svcIfc";
  private static final String TYPE_SERVICE_IMPLEMENTATION = "svcImpl";
  private static final String TYPE_SERVICE_REG_CLIENT = "svcClientReg";
  private static final String TYPE_SERVICE_REG_SERVER = "svcServerReg";

  private final BundleTreeWizardPage m_locationWizardPage;
  private final ProcessServiceNewWizardPage m_serviceNewWizardPage;
  private final ProcessServiceNewOperation m_operation;
  private final ITreeNode m_locationWizardPageRoot;
  private final IScoutBundle m_serverBundle;

  public ProcessServiceNewWizard(IScoutBundle serverBundle) {
    setWindowTitle(Texts.get("NewProcessService"));
    m_serverBundle = serverBundle;
    m_operation = new ProcessServiceNewOperation();
    P_StatusRevalidator statusProvider = new P_StatusRevalidator();

    m_serviceNewWizardPage = new ProcessServiceNewWizardPage(serverBundle);
    m_serviceNewWizardPage.addPropertyChangeListener(new P_LocationPropertyListener());
    addPage(m_serviceNewWizardPage);

    m_locationWizardPageRoot = createTree(serverBundle);
    m_locationWizardPage = new BundleTreeWizardPage(Texts.get("ProcessServiceLocation"), Texts.get("OrganiseLocations"), m_locationWizardPageRoot, new P_InitialCheckerFilter());
    m_locationWizardPage.addStatusProvider(statusProvider);
    m_locationWizardPage.addDndListener(new P_TreeDndListener());
    addPage(m_locationWizardPage);

    // init
    m_serviceNewWizardPage.setSuperType(RuntimeClasses.getSuperType(RuntimeClasses.IService, serverBundle.getJavaProject()));
  }

  private ITreeNode createTree(IScoutBundle serverBundle) {
    IScoutBundle sharedBundle = null;
    IScoutBundle clientBundle = null;
    if (serverBundle != null) {
      sharedBundle = serverBundle.getParentBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED), false);
      if (sharedBundle != null) {
        clientBundle = sharedBundle.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false);
      }
    }

    ITreeNode rootNode = TreeUtility.createBundleTree(serverBundle, NodeFilters.getByType(IScoutBundle.TYPE_CLIENT, IScoutBundle.TYPE_SERVER, IScoutBundle.TYPE_SHARED));

    if (clientBundle != null) {
      ITreeNode clientNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(clientBundle));
      // service client reg
      TreeUtility.createNode(clientNode, TYPE_SERVICE_REG_CLIENT, Texts.get("ServiceProxyRegistration"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Public));
    }
    if (sharedBundle != null) {
      ITreeNode sharedNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(sharedBundle));
      // permission create
      TreeUtility.createNode(sharedNode, TYPE_PERMISSION_CREATE, Texts.get("CreatePermission"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class));
      // permission read
      TreeUtility.createNode(sharedNode, TYPE_PERMISSION_READ, Texts.get("ReadPermission"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class));
      // permission update
      TreeUtility.createNode(sharedNode, TYPE_PERMISSION_UPDATE, Texts.get("UpdatePermission"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class));
      // service interface
      TreeUtility.createNode(sharedNode, TYPE_SERVICE_INTERFACE, Texts.get("IService"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Interface));
    }
    if (serverBundle != null) {
      ITreeNode serverNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(serverBundle));
      // service implementation
      TreeUtility.createNode(serverNode, TYPE_SERVICE_IMPLEMENTATION, Texts.get("Service"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class));
      // service implementation
      TreeUtility.createNode(serverNode, TYPE_SERVICE_REG_SERVER, Texts.get("ServiceRegistration"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Public));
    }
    return rootNode;
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    m_serviceNewWizardPage.fillProcessServiceNewOperation(m_operation);
    m_operation.setClientServiceRegistryBundles(m_locationWizardPage.getLocationBundles(TYPE_SERVICE_REG_CLIENT, true, true));

    IScoutBundle permissionBundle = m_locationWizardPage.getLocationBundle(TYPE_PERMISSION_CREATE, true, true);
    if (permissionBundle != null) {
      m_operation.setPermissionPackageName(permissionBundle.getPackageName(m_serviceNewWizardPage.getTargetPackage()));
      m_operation.setPermissionCreateBundle(permissionBundle);
      m_operation.setPermissionCreateName(m_locationWizardPage.getTextOfNode(TYPE_PERMISSION_CREATE, true, true));
      m_operation.setPermissionReadBundle(m_locationWizardPage.getLocationBundle(TYPE_PERMISSION_READ, true, true));
      m_operation.setPermissionReadName(m_locationWizardPage.getTextOfNode(TYPE_PERMISSION_READ, true, true));
      m_operation.setPermissionUpdateBundle(m_locationWizardPage.getLocationBundle(TYPE_PERMISSION_UPDATE, true, true));
      m_operation.setPermissionUpdateName(m_locationWizardPage.getTextOfNode(TYPE_PERMISSION_UPDATE, true, true));
    }
    m_operation.setServerServiceRegistryBundles(m_locationWizardPage.getLocationBundles(TYPE_SERVICE_REG_SERVER, true, true));

    IScoutBundle serviceImplBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
    if (serviceImplBundle != null) {
      m_operation.setServiceImplementationBundle(serviceImplBundle);
      m_operation.setServicePackageName(serviceImplBundle.getPackageName(m_serviceNewWizardPage.getTargetPackage()));
      m_operation.setServiceImplementationName(m_locationWizardPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION, true, true));
    }

    IScoutBundle serviceInterfaceBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
    if (serviceInterfaceBundle != null) {
      m_operation.setServiceInterfaceBundle(serviceInterfaceBundle);
      m_operation.setServiceInterfacePackageName(serviceInterfaceBundle.getPackageName(m_serviceNewWizardPage.getTargetPackage()));
      m_operation.setServiceInterfaceName(m_locationWizardPage.getTextOfNode(TYPE_SERVICE_INTERFACE, true, true));
    }
    return true;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    try {
      m_operation.validate();
      m_operation.run(monitor, workingCopyManager);
      return true;
    }
    catch (IllegalArgumentException e) {
      ScoutSdkUi.logWarning("validation error of operation '" + m_operation.getOperationName() + "'. " + e.getMessage());
      return false;
    }
    catch (CoreException e) {
      ScoutSdkUi.logError("error during executing operation '" + m_operation.getOperationName() + "'.", e);
      return false;
    }
  }

  private class P_LocationPropertyListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals(ServiceNewWizardPage.PROP_TYPE_NAME)) {
        String typeName = m_serviceNewWizardPage.getTypeName();
        if (!StringUtility.isNullOrEmpty(typeName)) {
          String prefix = typeName.replaceAll(SdkProperties.SUFFIX_SERVICE + "$", "");
          TreeUtility.findNode(m_locationWizardPageRoot, NodeFilters.getByType(TYPE_PERMISSION_CREATE)).setText("Create" + prefix + SdkProperties.SUFFIX_PERMISSION);
          TreeUtility.findNode(m_locationWizardPageRoot, NodeFilters.getByType(TYPE_PERMISSION_READ)).setText("Read" + prefix + SdkProperties.SUFFIX_PERMISSION);
          TreeUtility.findNode(m_locationWizardPageRoot, NodeFilters.getByType(TYPE_PERMISSION_UPDATE)).setText("Update" + prefix + SdkProperties.SUFFIX_PERMISSION);
          TreeUtility.findNode(m_locationWizardPageRoot, NodeFilters.getByType(TYPE_SERVICE_IMPLEMENTATION)).setText(prefix + SdkProperties.SUFFIX_SERVICE);
          TreeUtility.findNode(m_locationWizardPageRoot, NodeFilters.getByType(TYPE_SERVICE_INTERFACE)).setText("I" + prefix + SdkProperties.SUFFIX_SERVICE);
          m_locationWizardPage.refreshTree();
        }
        m_locationWizardPage.pingStateChanging();
      }
    }
  } // end class P_LocationPropertyListener

  private class P_InitialCheckerFilter implements ITreeNodeFilter {
    @Override
    public boolean accept(ITreeNode node) {
      return TreeUtility.isOneOf(node.getType(), TYPE_PERMISSION_CREATE, TYPE_PERMISSION_READ, TYPE_PERMISSION_UPDATE,
          TYPE_SERVICE_IMPLEMENTATION, TYPE_SERVICE_INTERFACE, TYPE_SERVICE_REG_CLIENT, TYPE_SERVICE_REG_SERVER);
    }
  } // end class P_InitialCheckerFilter

  private class P_TreeDndListener implements ITreeDndListener {
    @Override
    public boolean isDragableNode(ITreeNode node) {
      return TreeUtility.isOneOf(node.getType(), TYPE_PERMISSION_CREATE, TYPE_PERMISSION_READ, TYPE_PERMISSION_UPDATE,
          TYPE_SERVICE_IMPLEMENTATION, TYPE_SERVICE_INTERFACE, TYPE_SERVICE_REG_CLIENT, TYPE_SERVICE_REG_SERVER);
    }

    @Override
    public void validateTarget(DndEvent dndEvent) {
      if (dndEvent.targetParent == null) {
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
      m_serviceNewWizardPage.pingStateChanging();
    }

    private void validateDropCopy(DndEvent dndEvent) {
      String t = dndEvent.node.getType();
      if (TYPE_SERVICE_REG_CLIENT.equals(t)) {
        dndEvent.doit = IScoutBundle.TYPE_CLIENT.equals(dndEvent.targetParent.getType());
      }
      else if (TYPE_SERVICE_REG_SERVER.equals(t)) {
        dndEvent.doit = IScoutBundle.TYPE_SERVER.equals(dndEvent.targetParent.getType());
      }
      else {
        dndEvent.doit = false;
      }
    }

    private void validateDropMove(DndEvent dndEvent) {
      String t = dndEvent.node.getType();
      if (TYPE_SERVICE_REG_CLIENT.equals(t)) {
        dndEvent.doit = IScoutBundle.TYPE_CLIENT.equals(dndEvent.targetParent.getType());
      }
      else if (TreeUtility.isOneOf(t, TYPE_PERMISSION_CREATE, TYPE_PERMISSION_READ, TYPE_PERMISSION_UPDATE, TYPE_SERVICE_INTERFACE)) {
        dndEvent.doit = IScoutBundle.TYPE_SHARED.equals(dndEvent.targetParent.getType());
      }
      else if (TYPE_SERVICE_IMPLEMENTATION.equals(t) || TYPE_SERVICE_REG_SERVER.equals(t)) {
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
      multiStatus.add(getStatusTypeNames());
      multiStatus.add(getStatusService());
      multiStatus.add(getStatusServiceRegistrationClient());
      multiStatus.add(getStatusServiceRegistrationServer());
    }

    protected IStatus getStatusTypeNames() {
      IScoutBundle serviceImplementationBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
      if (serviceImplementationBundle != null) {
        ITreeNode serviceImplNode = m_locationWizardPage.getTreeNode(TYPE_SERVICE_IMPLEMENTATION, true, true);
        if (serviceImplNode != null) {
          try {
            String fqn = serviceImplementationBundle.getPackageName(m_serviceNewWizardPage.getTargetPackage()) + "." + serviceImplNode.getText();
            if (serviceImplementationBundle.getJavaProject().findType(fqn) != null) {
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + serviceImplNode.getText() + "' " + Texts.get("AlreadyExists") + ".");
            }
          }
          catch (JavaModelException e) {
            ScoutSdkUi.logError(e);
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("AnErrorOccured"));
          }
        }
      }
      IScoutBundle serviceInterfaceBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
      if (serviceInterfaceBundle != null) {
        ITreeNode serviceInterfaceNode = m_locationWizardPage.getTreeNode(TYPE_SERVICE_INTERFACE, true, true);
        if (serviceInterfaceNode != null) {
          try {
            String fqn = serviceInterfaceBundle.getPackageName(m_serviceNewWizardPage.getTargetPackage()) + "." + serviceInterfaceNode.getText();
            if (serviceInterfaceBundle.getJavaProject().findType(fqn) != null) {
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
      IScoutBundle permissionBundle = m_locationWizardPage.getLocationBundle(permissionType, true, true);
      if (permissionBundle != null) {
        ITreeNode permissionNode = m_locationWizardPage.getTreeNode(permissionType, true, true);
        if (permissionNode != null) {
          try {
            String fqn = permissionBundle.getPackageName(m_serviceNewWizardPage.getTargetPackage()) + "." + permissionNode.getText();
            if (permissionBundle.getJavaProject().findType(fqn) != null) {
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

    protected IStatus getStatusService() {
      IScoutBundle serviceImplementationBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
      if (serviceImplementationBundle != null) {
        IScoutBundle serviceInterfaceBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
        if (serviceInterfaceBundle != null) {
          if (!ScoutTypeUtility.isOnClasspath(serviceInterfaceBundle, serviceImplementationBundle)) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_locationWizardPage.getTextOfNode(TYPE_SERVICE_INTERFACE), m_locationWizardPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION)));
          }
        }
        IScoutBundle permissionCreateBundle = m_locationWizardPage.getLocationBundle(TYPE_PERMISSION_CREATE, true, true);
        if (permissionCreateBundle != null) {
          if (!ScoutTypeUtility.isOnClasspath(permissionCreateBundle, serviceImplementationBundle)) {
            return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_locationWizardPage.getTextOfNode(TYPE_PERMISSION_CREATE), m_locationWizardPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION)));
          }
        }
        IScoutBundle permissionReadBundle = m_locationWizardPage.getLocationBundle(TYPE_PERMISSION_READ, true, true);
        if (permissionReadBundle != null) {
          if (!ScoutTypeUtility.isOnClasspath(permissionReadBundle, serviceImplementationBundle)) {
            return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_locationWizardPage.getTextOfNode(TYPE_PERMISSION_READ), m_locationWizardPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION)));
          }
        }
        IScoutBundle permissionUpdateBundle = m_locationWizardPage.getLocationBundle(TYPE_PERMISSION_UPDATE, true, true);
        if (permissionUpdateBundle != null) {
          if (!ScoutTypeUtility.isOnClasspath(permissionUpdateBundle, serviceImplementationBundle)) {
            return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_locationWizardPage.getTextOfNode(TYPE_PERMISSION_UPDATE), m_locationWizardPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION)));
          }
        }
      }
      return Status.OK_STATUS;
    }

    protected IStatus getStatusServiceRegistrationClient() {
      IScoutBundle serviceInterfaceBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
      ITreeNode[] serviceRegistrationClientNodes = m_locationWizardPage.getTreeNodes(TYPE_SERVICE_REG_CLIENT, true, true);
      for (ITreeNode serviceRegNode : serviceRegistrationClientNodes) {
        Object data = serviceRegNode.getParent().getData();
        if (data instanceof IScoutBundle) {
          IScoutBundle serviceRegistrationBundle = (IScoutBundle) data;
          if (serviceInterfaceBundle != null && serviceRegistrationBundle != null) {
            if (!ScoutTypeUtility.isOnClasspath(serviceInterfaceBundle, serviceRegistrationBundle)) {
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotOnClasspathOfServiceY", m_locationWizardPage.getTextOfNode(TYPE_SERVICE_INTERFACE), serviceRegistrationBundle.getSymbolicName()));
            }
          }
        }
      }
      return Status.OK_STATUS;
    }

    protected IStatus getStatusServiceRegistrationServer() {
      IScoutBundle serviceImplementationBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
      ITreeNode[] serviceRegistrationServerNodes = m_locationWizardPage.getTreeNodes(TYPE_SERVICE_REG_SERVER, true, true);
      for (ITreeNode serviceRegNode : serviceRegistrationServerNodes) {
        Object data = serviceRegNode.getParent().getData();
        if (data instanceof IScoutBundle) {
          IScoutBundle serviceRegistrationBundle = (IScoutBundle) data;
          if (serviceImplementationBundle != null && serviceRegistrationBundle != null) {
            if (!ScoutTypeUtility.isOnClasspath(serviceImplementationBundle, serviceRegistrationBundle)) {
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotOnClasspathOfServiceY", m_locationWizardPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION), serviceRegistrationBundle.getSymbolicName()));
            }
          }
        }
      }
      return Status.OK_STATUS;
    }
  } // end class P_StatusRevalidator
}
