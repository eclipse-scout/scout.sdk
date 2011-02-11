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
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.operation.form.FormStackNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.bundletree.DndEvent;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeDndListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNodeFilter;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.ui.wizard.BundleTreeWizardPage;
import org.eclipse.scout.sdk.ui.wizard.IStatusProvider;
import org.eclipse.scout.sdk.ui.wizard.services.ServiceNewWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.swt.dnd.DND;

/**
 * <h3> {@link FormNewWizard}</h3> To create a new form.
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 05.11.2009
 */
public class FormNewWizard extends AbstractWorkspaceWizard {

  public static final int TYPE_FORM = 100;
  public static final int TYPE_FORM_BUTTON_OK = 101;
  public static final int TYPE_FORM_BUTTON_CANCEL = 102;
  public static final int TYPE_HANDLER_MODIFY = 110;
  public static final int TYPE_HANDLER_NEW = 111;
  public static final int TYPE_FORM_DATA = 130;
  public static final int TYPE_PERMISSION_CREATE = 140;
  public static final int TYPE_PERMISSION_READ = 143;
  public static final int TYPE_PERMISSION_UPDATE = 146;
  public static final int TYPE_SERVICE_INTERFACE = 200;
  public static final int TYPE_SERVICE_IMPLEMENTATION = 210;
  public static final int TYPE_SERVICE_REG_CLIENT = 230;
  public static final int TYPE_SERVICE_REG_SERVER = 240;

  private final IScoutBundle m_clientBundle;
  private FormNewWizardPage m_formPage;
  private BundleTreeWizardPage m_locationPage;
  private FormStackNewOperation m_operation = new FormStackNewOperation(true);
  private ITreeNode m_locationPageRoot;

  public FormNewWizard(IScoutBundle clientBundle) {
    P_StatusRevalidator statusProvider = new P_StatusRevalidator();
    m_clientBundle = clientBundle;
    m_formPage = new FormNewWizardPage(getClientBundle());
    addPage(m_formPage);
    m_locationPageRoot = createTree(clientBundle);
    m_locationPage = new BundleTreeWizardPage("Form Class Locations", "Use drag'n drop to organise the locations.\n Only selected items will be created.", m_locationPageRoot, new P_InitialCheckedFilter());
    m_locationPage.addStatusProvider(statusProvider);
    m_locationPage.addDndListener(new P_TreeDndListener());
    addPage(m_locationPage);
    // init
    m_formPage.addPropertyChangeListener(new P_LocationPropertyListener());
  }

  private ITreeNode createTree(IScoutBundle clientBundle) {
    IScoutBundle sharedBundle = null;
    IScoutBundle serverBundle = null;
    IScoutProject scoutProject = clientBundle.getScoutProject();
    while ((sharedBundle == null || serverBundle == null) && scoutProject != null) {
      sharedBundle = scoutProject.getSharedBundle();
      serverBundle = scoutProject.getServerBundle();
      scoutProject = scoutProject.getParentProject();
    }

    // BundleSet bundleSet=BundleLocationUtility.createBundleSet(clientBundle);
    ITreeNode rootNode = TreeUtility.createBundleTree(clientBundle.getScoutProject(), NodeFilters.getAcceptAll());

    ITreeNode clientNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(getClientBundle()));
    // form
    ITreeNode formNode = TreeUtility.createNode(clientNode, TYPE_FORM, "Form", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), TYPE_FORM);
    formNode.setEnabled(false);
    // form buttons
    TreeUtility.createNode(formNode, TYPE_FORM_BUTTON_OK, "Ok Button", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), TYPE_FORM_BUTTON_OK);
    TreeUtility.createNode(formNode, TYPE_FORM_BUTTON_CANCEL, "Cancel Button", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), TYPE_FORM_BUTTON_CANCEL);

    // newHandler
    TreeUtility.createNode(formNode, TYPE_HANDLER_NEW, "NewHandler", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), TYPE_HANDLER_NEW);
    // modifyHandler
    TreeUtility.createNode(formNode, TYPE_HANDLER_MODIFY, "ModifyHandler", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), TYPE_HANDLER_MODIFY);
    if (sharedBundle != null && serverBundle != null) {
      // service client reg
      TreeUtility.createNode(clientNode, TYPE_SERVICE_REG_CLIENT, "Service Proxy Registration", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Public), TYPE_SERVICE_REG_CLIENT);
    }

    if (sharedBundle != null) {
      ITreeNode sharedNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(sharedBundle));
      // formData
      TreeUtility.createNode(sharedNode, TYPE_FORM_DATA, "FormData", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), TYPE_FORM_DATA);
      // permission create
      TreeUtility.createNode(sharedNode, TYPE_PERMISSION_CREATE, "CreatePermission", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), TYPE_PERMISSION_CREATE);
      // permission read
      TreeUtility.createNode(sharedNode, TYPE_PERMISSION_READ, "ReadPermission", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), TYPE_PERMISSION_READ);
      // permission update
      TreeUtility.createNode(sharedNode, TYPE_PERMISSION_UPDATE, "UpdatePermission", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), TYPE_PERMISSION_UPDATE);
      if (serverBundle != null) {
        // service interface
        TreeUtility.createNode(sharedNode, TYPE_SERVICE_INTERFACE, "IService", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Interface), TYPE_SERVICE_INTERFACE);
      }
    }
    if (serverBundle != null) {
      ITreeNode serverNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(serverBundle));
      // service implementation
      TreeUtility.createNode(serverNode, TYPE_SERVICE_IMPLEMENTATION, "Service", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), TYPE_SERVICE_IMPLEMENTATION);
      // service implementation
      TreeUtility.createNode(serverNode, TYPE_SERVICE_REG_SERVER, "Service Registration", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Public), TYPE_SERVICE_REG_SERVER);
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
    m_operation.setFormBundle(m_locationPage.getLocationBundle(TYPE_FORM, true, true));
    m_operation.setFormDataBundle(m_locationPage.getLocationBundle(TYPE_FORM_DATA, true, true));

    m_operation.setClientServiceRegistryBundles(m_locationPage.getLocationBundles(TYPE_SERVICE_REG_CLIENT, true, true));
    m_operation.setPermissionCreateBundle(m_locationPage.getLocationBundle(TYPE_PERMISSION_CREATE, true, true));
    m_operation.setPermissionCreateName(m_locationPage.getTextOfNode(TYPE_PERMISSION_CREATE, true, true));
    m_operation.setPermissionReadBundle(m_locationPage.getLocationBundle(TYPE_PERMISSION_READ, true, true));
    m_operation.setPermissionReadName(m_locationPage.getTextOfNode(TYPE_PERMISSION_READ, true, true));
    m_operation.setPermissionUpdateBundle(m_locationPage.getLocationBundle(TYPE_PERMISSION_UPDATE, true, true));
    m_operation.setPermissionUpdateName(m_locationPage.getTextOfNode(TYPE_PERMISSION_UPDATE, true, true));
    m_operation.setServerServiceRegistryBundles(m_locationPage.getLocationBundles(TYPE_SERVICE_REG_SERVER, true, true));
    m_operation.setServiceImplementationBundle(m_locationPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true));
    m_operation.setServiceImplementationName(m_locationPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION, true, true));
    m_operation.setServiceInterfaceBundle(m_locationPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true));
    m_operation.setServiceInterfaceName(m_locationPage.getTextOfNode(TYPE_SERVICE_INTERFACE, true, true));
    return true;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) {
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
      if (evt.getPropertyName().equals(ServiceNewWizardPage.PROP_TYPE_NAME)) {
        String typeName = m_formPage.getTypeName();
        if (!StringUtility.isNullOrEmpty(typeName)) {
          String prefix = typeName.replaceAll(ScoutIdeProperties.SUFFIX_FORM + "$", "");
          TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_FORM)).setText(prefix + ScoutIdeProperties.SUFFIX_FORM);
          ITreeNode formDataNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_FORM_DATA));
          if (formDataNode != null) {
            formDataNode.setText(prefix + ScoutIdeProperties.SUFFIX_FORM_DATA);
          }
          ITreeNode permissionCreateNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_PERMISSION_CREATE));
          if (permissionCreateNode != null) {
            permissionCreateNode.setText("Create" + prefix + ScoutIdeProperties.SUFFIX_PERMISSION);
          }
          ITreeNode permissionReadNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_PERMISSION_READ));
          if (permissionReadNode != null) {
            permissionReadNode.setText("Read" + prefix + ScoutIdeProperties.SUFFIX_PERMISSION);
          }

          ITreeNode permissionUpdateNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_PERMISSION_UPDATE));
          if (permissionUpdateNode != null) {
            permissionUpdateNode.setText("Update" + prefix + ScoutIdeProperties.SUFFIX_PERMISSION);
          }
          ITreeNode serviceImplNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SERVICE_IMPLEMENTATION));
          if (serviceImplNode != null) {
            serviceImplNode.setText(prefix + ScoutIdeProperties.SUFFIX_PROCESS_SERVICE);
          }
          ITreeNode serviceInterfaceNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SERVICE_INTERFACE));
          if (serviceInterfaceNode != null) {
            serviceInterfaceNode.setText("I" + prefix + ScoutIdeProperties.SUFFIX_PROCESS_SERVICE);
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
      switch (node.getType()) {
        case IScoutBundle.BUNDLE_CLIENT:
        case IScoutBundle.BUNDLE_SHARED:
        case IScoutBundle.BUNDLE_SERVER:
        case TYPE_HANDLER_MODIFY:
        case TYPE_HANDLER_NEW:
        case TYPE_PERMISSION_CREATE:
        case TYPE_PERMISSION_READ:
        case TYPE_PERMISSION_UPDATE:
        case TYPE_SERVICE_IMPLEMENTATION:
        case TYPE_SERVICE_INTERFACE:
        case TYPE_SERVICE_REG_CLIENT:
        case TYPE_SERVICE_REG_SERVER:
        case TYPE_FORM:
        case TYPE_FORM_BUTTON_OK:
        case TYPE_FORM_BUTTON_CANCEL:
        case TYPE_FORM_DATA:
          return true;
        default:
          return false;
      }
    }
  } // end class P_InitialCheckedFilter

  private class P_TreeDndListener implements ITreeDndListener {
    @Override
    public boolean isDragableNode(ITreeNode node) {
      switch (node.getType()) {
        case TYPE_FORM:
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
      if (dndEvent.node.getType() == TYPE_FORM) {
        ITreeNode formDataNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_FORM_DATA));
        if (formDataNode != null) {

          ITreeNode oldFomDataParent = formDataNode.getParent();
          IScoutBundle formBundle = (IScoutBundle) dndEvent.node.getParent().getData();
          IScoutBundle[] sharedBundles = formBundle.getRequiredBundles(ScoutBundleFilters.getSharedFilter(), false);
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
        case TYPE_FORM:
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
          String fqn = serviceImplementationBundle.getPackageName(IScoutBundle.SERVER_PACKAGE_APPENDIX_SERVICES_PROCESS) + "." + serviceImplNode.getText();
          IType findType = serviceImplementationBundle.findType(fqn);
          if (findType != null && findType.exists()) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + serviceImplNode.getText() + "' already exists.");
          }
        }
      }
      // shared bundles
      IScoutBundle serviceInterfaceBundle = m_locationPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
      if (serviceInterfaceBundle != null) {
        ITreeNode serviceInterfaceNode = m_locationPage.getTreeNode(TYPE_SERVICE_INTERFACE, true, true);
        if (serviceInterfaceNode != null) {
          String fqn = serviceInterfaceBundle.getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES_PROCESS) + "." + serviceInterfaceNode.getText();
          IType interfaceType = serviceInterfaceBundle.findType(fqn);
          if (interfaceType != null && interfaceType.exists()) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + serviceInterfaceNode.getText() + "' already exists.");
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
      IScoutBundle permissionBundle = m_locationPage.getLocationBundle(permissionType, true, true);
      if (permissionBundle != null) {
        ITreeNode permissionNode = m_locationPage.getTreeNode(permissionType, true, true);
        if (permissionNode != null) {
          String fqn = permissionBundle.getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SECURITY) + "." + permissionNode.getText();
          IType permission = permissionBundle.findType(fqn);
          if (permission != null && permission.exists()) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + permissionNode.getText() + "' already exists.");
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
          if (!formBundle.isOnClasspath(formDataBundle)) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + m_locationPage.getTextOfNode(TYPE_FORM_DATA) + " is not on classpath of '" + m_locationPage.getTextOfNode(TYPE_FORM) + "'.");
          }
        }
        IScoutBundle formServiceBundle = m_locationPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
        if (formServiceBundle != null) {
          if (!formBundle.isOnClasspath(formServiceBundle)) {
            return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, "'" + m_locationPage.getTextOfNode(TYPE_SERVICE_INTERFACE) + " is not on classpath of '" + m_locationPage.getTextOfNode(TYPE_FORM) + "'.");
          }
        }
        IScoutBundle permissionCreateBundle = m_locationPage.getLocationBundle(TYPE_PERMISSION_CREATE, true, true);
        if (permissionCreateBundle != null) {
          if (!formBundle.isOnClasspath(permissionCreateBundle)) {
            return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, "'" + m_locationPage.getTextOfNode(TYPE_PERMISSION_CREATE) + " is not on classpath of '" + m_locationPage.getTextOfNode(TYPE_FORM) + "'.");
          }
        }
        IScoutBundle permissionReadBundle = m_locationPage.getLocationBundle(TYPE_PERMISSION_READ, true, true);
        if (permissionReadBundle != null) {
          if (!formBundle.isOnClasspath(permissionReadBundle)) {
            return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, "'" + m_locationPage.getTextOfNode(TYPE_PERMISSION_READ) + " is not on classpath of '" + m_locationPage.getTextOfNode(TYPE_FORM) + "'.");
          }
        }
        IScoutBundle permissionUpdateBundle = m_locationPage.getLocationBundle(TYPE_PERMISSION_UPDATE, true, true);
        if (permissionUpdateBundle != null) {
          if (!formBundle.isOnClasspath(permissionUpdateBundle)) {
            return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, "'" + m_locationPage.getTextOfNode(TYPE_PERMISSION_UPDATE) + " is not on classpath of '" + m_locationPage.getTextOfNode(TYPE_FORM) + "'.");
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
          if (!serviceImplementationBundle.isOnClasspath(serviceInterfaceBundle)) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + m_locationPage.getTextOfNode(TYPE_SERVICE_INTERFACE) + " is not on classpath of '" + m_locationPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION) + "'.");
          }
        }
        IScoutBundle permissionCreateBundle = m_locationPage.getLocationBundle(TYPE_PERMISSION_CREATE, true, true);
        if (permissionCreateBundle != null) {
          if (!serviceImplementationBundle.isOnClasspath(permissionCreateBundle)) {
            return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, "'" + m_locationPage.getTextOfNode(TYPE_PERMISSION_CREATE) + " is not on classpath of '" + m_locationPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION) + "'.");
          }
        }
        IScoutBundle permissionReadBundle = m_locationPage.getLocationBundle(TYPE_PERMISSION_READ, true, true);
        if (permissionReadBundle != null) {
          if (!serviceImplementationBundle.isOnClasspath(permissionReadBundle)) {
            return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, "'" + m_locationPage.getTextOfNode(TYPE_PERMISSION_READ) + " is not on classpath of '" + m_locationPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION) + "'.");
          }
        }
        IScoutBundle permissionUpdateBundle = m_locationPage.getLocationBundle(TYPE_PERMISSION_UPDATE, true, true);
        if (permissionUpdateBundle != null) {
          if (!serviceImplementationBundle.isOnClasspath(permissionUpdateBundle)) {
            return new Status(IStatus.WARNING, ScoutSdkUi.PLUGIN_ID, "'" + m_locationPage.getTextOfNode(TYPE_PERMISSION_UPDATE) + " is not on classpath of '" + m_locationPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION) + "'.");
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
          if (serviceInterfaceBundle != null && serviceRegistrationBundle != null) {
            if (!serviceRegistrationBundle.isOnClasspath(serviceInterfaceBundle)) {
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + m_locationPage.getTextOfNode(TYPE_SERVICE_INTERFACE) + " is not on classpath of Service Registration in '" + serviceRegistrationBundle.getBundleName() + "'.");
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
            if (!serviceRegistrationBundle.isOnClasspath(serviceImplementationBundle)) {
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + m_locationPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION) + " is not on classpath of Service Registration in '" + serviceRegistrationBundle.getBundleName() + "'.");
            }
          }
        }
      }

      return Status.OK_STATUS;
    }

  } // end class P_StatusRevalidator

}
