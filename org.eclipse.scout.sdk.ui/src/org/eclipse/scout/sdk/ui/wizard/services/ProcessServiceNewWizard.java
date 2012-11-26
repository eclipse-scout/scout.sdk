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
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
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
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.dnd.DND;

public class ProcessServiceNewWizard extends AbstractWorkspaceWizard {
  private static final int TYPE_PERMISSION_CREATE = 104;
  private static final int TYPE_PERMISSION_READ = 105;
  private static final int TYPE_PERMISSION_UPDATE = 106;
  private static final int TYPE_SERVICE_INTERFACE = 107;
  private static final int TYPE_SERVICE_IMPLEMENTATION = 108;
  private static final int TYPE_SERVICE_REG_CLIENT = 109;
  private static final int TYPE_SERVICE_REG_SERVER = 110;

  private BundleTreeWizardPage m_locationWizardPage;
  private ProcessServiceNewWizardPage m_serviceNewWizardPage;
  private ProcessServiceNewOperation m_operation = new ProcessServiceNewOperation();
  private ITreeNode m_locationWizardPageRoot;

  public ProcessServiceNewWizard(IScoutBundle serverBundle) {
    setWindowTitle(Texts.get("NewProcessService"));
    P_StatusRevalidator statusProvider = new P_StatusRevalidator();
    m_serviceNewWizardPage = new ProcessServiceNewWizardPage();
    m_serviceNewWizardPage.setServerBundle(serverBundle);
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
    IScoutProject scoutProject = serverBundle.getScoutProject();
    while ((sharedBundle == null || clientBundle == null) && scoutProject != null) {
      sharedBundle = scoutProject.getSharedBundle();
      clientBundle = scoutProject.getClientBundle();
      scoutProject = scoutProject.getParentProject();
    }
    ITreeNode rootNode = TreeUtility.createBundleTree(serverBundle.getScoutProject(), NodeFilters.getAcceptAll());
    if (clientBundle != null) {
      ITreeNode clientNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(clientBundle));
      // service client reg
      TreeUtility.createNode(clientNode, TYPE_SERVICE_REG_CLIENT, Texts.get("ServiceProxyRegistration"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Public), TYPE_SERVICE_REG_CLIENT);
    }
    if (sharedBundle != null) {
      ITreeNode sharedNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(sharedBundle));
      // permission create
      TreeUtility.createNode(sharedNode, TYPE_PERMISSION_CREATE, Texts.get("CreatePermission"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), TYPE_PERMISSION_CREATE);
      // permission read
      TreeUtility.createNode(sharedNode, TYPE_PERMISSION_READ, Texts.get("ReadPermission"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), TYPE_PERMISSION_READ);
      // permission update
      TreeUtility.createNode(sharedNode, TYPE_PERMISSION_UPDATE, Texts.get("UpdatePermission"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), TYPE_PERMISSION_UPDATE);
      // service interface
      TreeUtility.createNode(sharedNode, TYPE_SERVICE_INTERFACE, Texts.get("IService"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Interface), TYPE_SERVICE_INTERFACE);
    }
    if (serverBundle != null) {
      ITreeNode serverNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(serverBundle));
      // service implementation
      TreeUtility.createNode(serverNode, TYPE_SERVICE_IMPLEMENTATION, Texts.get("Service"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), TYPE_SERVICE_IMPLEMENTATION);
      // service implementation
      TreeUtility.createNode(serverNode, TYPE_SERVICE_REG_SERVER, Texts.get("ServiceRegistration"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Public), TYPE_SERVICE_REG_SERVER);
    }
    return rootNode;
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    m_serviceNewWizardPage.fillProcessServiceNewOperation(m_operation);
    m_operation.setClientServiceRegistryBundles(m_locationWizardPage.getLocationBundles(TYPE_SERVICE_REG_CLIENT, true, true));
    m_operation.setPermissionCreateBundle(m_locationWizardPage.getLocationBundle(TYPE_PERMISSION_CREATE, true, true));
    m_operation.setPermissionCreateName(m_locationWizardPage.getTextOfNode(TYPE_PERMISSION_CREATE, true, true));
    m_operation.setPermissionReadBundle(m_locationWizardPage.getLocationBundle(TYPE_PERMISSION_READ, true, true));
    m_operation.setPermissionReadName(m_locationWizardPage.getTextOfNode(TYPE_PERMISSION_READ, true, true));
    m_operation.setPermissionUpdateBundle(m_locationWizardPage.getLocationBundle(TYPE_PERMISSION_UPDATE, true, true));
    m_operation.setPermissionUpdateName(m_locationWizardPage.getTextOfNode(TYPE_PERMISSION_UPDATE, true, true));
    m_operation.setServerServiceRegistryBundles(m_locationWizardPage.getLocationBundles(TYPE_SERVICE_REG_SERVER, true, true));
    m_operation.setServiceImplementationBundle(m_locationWizardPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true));
    m_operation.setServiceImplementationName(m_locationWizardPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION, true, true));
    m_operation.setServiceInterfaceBundle(m_locationWizardPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true));
    m_operation.setServiceInterfaceName(m_locationWizardPage.getTextOfNode(TYPE_SERVICE_INTERFACE, true, true));

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
          String prefix = typeName.replaceAll(SdkProperties.SUFFIX_PROCESS_SERVICE + "$", "");
          TreeUtility.findNode(m_locationWizardPageRoot, NodeFilters.getByType(TYPE_PERMISSION_CREATE)).setText("Create" + prefix + SdkProperties.SUFFIX_PERMISSION);
          TreeUtility.findNode(m_locationWizardPageRoot, NodeFilters.getByType(TYPE_PERMISSION_READ)).setText("Read" + prefix + SdkProperties.SUFFIX_PERMISSION);
          TreeUtility.findNode(m_locationWizardPageRoot, NodeFilters.getByType(TYPE_PERMISSION_UPDATE)).setText("Update" + prefix + SdkProperties.SUFFIX_PERMISSION);
          TreeUtility.findNode(m_locationWizardPageRoot, NodeFilters.getByType(TYPE_SERVICE_IMPLEMENTATION)).setText(prefix + SdkProperties.SUFFIX_PROCESS_SERVICE);
          TreeUtility.findNode(m_locationWizardPageRoot, NodeFilters.getByType(TYPE_SERVICE_INTERFACE)).setText("I" + prefix + SdkProperties.SUFFIX_PROCESS_SERVICE);
          m_locationWizardPage.refreshTree();
        }
        m_locationWizardPage.pingStateChanging();
      }
    }
  } // end class P_LocationPropertyListener

  private class P_InitialCheckerFilter implements ITreeNodeFilter {
    @Override
    public boolean accept(ITreeNode node) {
      switch (node.getType()) {
        case TYPE_PERMISSION_CREATE:
        case TYPE_PERMISSION_READ:
        case TYPE_PERMISSION_UPDATE:
        case TYPE_SERVICE_IMPLEMENTATION:
        case TYPE_SERVICE_INTERFACE:
        case TYPE_SERVICE_REG_CLIENT:
        case TYPE_SERVICE_REG_SERVER:
          return true;
        case IScoutBundle.BUNDLE_CLIENT:
        case IScoutBundle.BUNDLE_SHARED:
        case IScoutBundle.BUNDLE_SERVER:
        default:
          return false;
      }
    }
  } // end class P_InitialCheckerFilter

  private class P_TreeDndListener implements ITreeDndListener {
    @Override
    public boolean isDragableNode(ITreeNode node) {
      switch (node.getType()) {
        case TYPE_PERMISSION_CREATE:
        case TYPE_PERMISSION_READ:
        case TYPE_PERMISSION_UPDATE:
        case TYPE_SERVICE_IMPLEMENTATION:
        case TYPE_SERVICE_INTERFACE:
        case TYPE_SERVICE_REG_CLIENT:
        case TYPE_SERVICE_REG_SERVER:
          return true;
        default:
          return false;
      }
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
      switch (dndEvent.node.getType()) {
        case TYPE_SERVICE_REG_CLIENT:
          dndEvent.doit = dndEvent.targetParent.getType() == IScoutBundle.BUNDLE_CLIENT;
          break;
        case TYPE_SERVICE_REG_SERVER:
          dndEvent.doit = dndEvent.targetParent.getType() == IScoutBundle.BUNDLE_SERVER;
          break;
        default:
          dndEvent.doit = false;
          break;
      }
    }

    private void validateDropMove(DndEvent dndEvent) {
      switch (dndEvent.node.getType()) {
        case TYPE_SERVICE_REG_CLIENT:
          dndEvent.doit = dndEvent.targetParent.getType() == IScoutBundle.BUNDLE_CLIENT;
          break;
        case TYPE_PERMISSION_CREATE:
        case TYPE_PERMISSION_READ:
        case TYPE_PERMISSION_UPDATE:
        case TYPE_SERVICE_INTERFACE:
          dndEvent.doit = dndEvent.targetParent.getType() == IScoutBundle.BUNDLE_SHARED;
          break;
        case TYPE_SERVICE_IMPLEMENTATION:
        case TYPE_SERVICE_REG_SERVER:
          dndEvent.doit = dndEvent.targetParent.getType() == IScoutBundle.BUNDLE_SERVER;
          break;
        default:
          dndEvent.doit = false;
          break;
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
          String fqn = serviceImplementationBundle.getPackageName(IScoutBundle.SERVER_PACKAGE_APPENDIX_SERVICES_PROCESS) + "." + serviceImplNode.getText();
          if (serviceImplementationBundle.findType(fqn) != null) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + serviceImplNode.getText() + "' " + Texts.get("AlreadyExists") + ".");
          }
        }
      }
      IScoutBundle serviceInterfaceBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
      if (serviceInterfaceBundle != null) {
        ITreeNode serviceInterfaceNode = m_locationWizardPage.getTreeNode(TYPE_SERVICE_INTERFACE, true, true);
        if (serviceInterfaceNode != null) {
          String fqn = serviceInterfaceBundle.getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES_PROCESS) + "." + serviceInterfaceNode.getText();
          if (serviceInterfaceBundle.findType(fqn) != null) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + serviceInterfaceNode.getText() + "' " + Texts.get("AlreadyExists") + ".");
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

    protected IStatus getStatusPermission(int permissionType) {
      IScoutBundle permissionBundle = m_locationWizardPage.getLocationBundle(permissionType, true, true);
      if (permissionBundle != null) {
        ITreeNode permissionNode = m_locationWizardPage.getTreeNode(permissionType, true, true);
        if (permissionNode != null) {
          String fqn = permissionBundle.getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SECURITY) + "." + permissionNode.getText();
          if (permissionBundle.findType(fqn) != null) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + permissionNode.getText() + "' " + Texts.get("AlreadyExists") + ".");
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
          if (!serviceImplementationBundle.isOnClasspath(serviceInterfaceBundle)) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_locationWizardPage.getTextOfNode(TYPE_SERVICE_INTERFACE), m_locationWizardPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION)));
          }
        }
        IScoutBundle permissionCreateBundle = m_locationWizardPage.getLocationBundle(TYPE_PERMISSION_CREATE, true, true);
        if (permissionCreateBundle != null) {
          if (!serviceImplementationBundle.isOnClasspath(permissionCreateBundle)) {
            return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_locationWizardPage.getTextOfNode(TYPE_PERMISSION_CREATE), m_locationWizardPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION)));
          }
        }
        IScoutBundle permissionReadBundle = m_locationWizardPage.getLocationBundle(TYPE_PERMISSION_READ, true, true);
        if (permissionReadBundle != null) {
          if (!serviceImplementationBundle.isOnClasspath(permissionReadBundle)) {
            return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_locationWizardPage.getTextOfNode(TYPE_PERMISSION_READ), m_locationWizardPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION)));
          }
        }
        IScoutBundle permissionUpdateBundle = m_locationWizardPage.getLocationBundle(TYPE_PERMISSION_UPDATE, true, true);
        if (permissionUpdateBundle != null) {
          if (!serviceImplementationBundle.isOnClasspath(permissionUpdateBundle)) {
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
            if (!serviceRegistrationBundle.isOnClasspath(serviceInterfaceBundle)) {
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotOnClasspathOfServiceY", m_locationWizardPage.getTextOfNode(TYPE_SERVICE_INTERFACE), serviceRegistrationBundle.getBundleName()));
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
            if (!serviceRegistrationBundle.isOnClasspath(serviceImplementationBundle)) {
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotOnClasspathOfServiceY", m_locationWizardPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION), serviceRegistrationBundle.getBundleName()));
            }
          }
        }
      }

      return Status.OK_STATUS;
    }

  } // end class P_StatusRevalidator
}
