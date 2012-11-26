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
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.service.ServiceNewOperation;
import org.eclipse.scout.sdk.ui.fields.bundletree.DndEvent;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeDndListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.ui.wizard.BundleTreeWizardPage;
import org.eclipse.scout.sdk.ui.wizard.IStatusProvider;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.dnd.DND;

public class ClientServiceNewWizard extends AbstractWorkspaceWizard {

  public static final int TYPE_SERVICE_INTERFACE = 107;
  public static final int TYPE_SERVICE_IMPLEMENTATION = 108;
  public static final int TYPE_SERVICE_REGISTRATION = 109;

  private BundleTreeWizardPage m_locationWizardPage;
  private ServiceNewWizardPage m_serviceNewWizardPage;
  private ServiceNewOperation m_operation = new ServiceNewOperation();
  private ITreeNode m_locationPageRoot;

  public ClientServiceNewWizard(IScoutBundle clientBundle) {
    setWindowTitle(Texts.get("NewClientService"));
    P_StatusRevalidator statusProvider = new P_StatusRevalidator();
    m_serviceNewWizardPage = new ServiceNewWizardPage(Texts.get("NewClientService"), Texts.get("CreateANewClientService"), TypeUtility.getType(RuntimeClasses.IService), SdkProperties.SUFFIX_SERVICE);
    m_serviceNewWizardPage.setLocationBundle(clientBundle);
    m_serviceNewWizardPage.addStatusProvider(statusProvider);
    m_serviceNewWizardPage.addPropertyChangeListener(new P_LocationPropertyListener());
    addPage(m_serviceNewWizardPage);
    m_locationPageRoot = createTree(clientBundle);
    m_locationWizardPage = new BundleTreeWizardPage(Texts.get("ServiceLocation"), Texts.get("OrganiseLocations"), m_locationPageRoot, NodeFilters.getByData((Object[]) null));
    m_locationWizardPage.addStatusProvider(statusProvider);
    m_locationWizardPage.addDndListener(new P_TreeDndListener());
    addPage(m_locationWizardPage);
    // init
    m_serviceNewWizardPage.setSuperType(RuntimeClasses.getSuperType(RuntimeClasses.IService, clientBundle.getJavaProject()));
  }

  private ITreeNode createTree(IScoutBundle clientBundle) {
    ITreeNode rootNode = TreeUtility.createBundleTree(clientBundle.getScoutProject(), NodeFilters.getByType(IScoutBundle.BUNDLE_CLIENT));
    ITreeNode clientNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(clientBundle));
    // service client reg
    TreeUtility.createNode(clientNode, TYPE_SERVICE_REGISTRATION, Texts.get("ServiceRegistration"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Public), TYPE_SERVICE_IMPLEMENTATION);
    TreeUtility.createNode(clientNode, TYPE_SERVICE_INTERFACE, Texts.get("IService"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Interface), TYPE_SERVICE_INTERFACE);
    TreeUtility.createNode(clientNode, TYPE_SERVICE_IMPLEMENTATION, Texts.get("Service"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), TYPE_SERVICE_IMPLEMENTATION);
    return rootNode;
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    IScoutBundle implementationBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
    if (implementationBundle != null) {
      m_operation.setImplementationBundle(implementationBundle);
      m_operation.setServicePackageName(implementationBundle.getPackageName(IScoutBundle.CLIENT_PACKAGE_APPENDIX_SERVICES));
    }
    IScoutBundle interfaceBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
    if (interfaceBundle != null) {
      m_operation.setInterfaceBundle(interfaceBundle);
      m_operation.setServiceInterfacePackageName(interfaceBundle.getPackageName(IScoutBundle.CLIENT_PACKAGE_APPENDIX_SERVICES));
    }
    m_operation.setServiceInterfaceName(m_locationWizardPage.getTextOfNode(TYPE_SERVICE_INTERFACE, true, true));
    m_operation.setServiceInterfaceSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.IService));
    IType superType = m_serviceNewWizardPage.getSuperType();
    if (superType != null) {
      m_operation.setServiceSuperTypeSignature(SignatureCache.createTypeSignature(superType.getFullyQualifiedName()));
    }
    m_operation.setServiceName(m_locationWizardPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION, true, true));
    for (IScoutBundle sb : m_locationWizardPage.getLocationBundles(TYPE_SERVICE_REGISTRATION, true, true)) {
      m_operation.addServiceRegistrationBundle(sb);
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
          TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SERVICE_IMPLEMENTATION)).setText(prefix + SdkProperties.SUFFIX_SERVICE);
          TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SERVICE_INTERFACE)).setText("I" + prefix + SdkProperties.SUFFIX_SERVICE);
          m_locationWizardPage.refreshTree();
        }
        m_locationWizardPage.pingStateChanging();
        m_serviceNewWizardPage.pingStateChanging();
      }
    }
  } // end class P_LocationPropertyListener

  private class P_TreeDndListener implements ITreeDndListener {
    @Override
    public boolean isDragableNode(ITreeNode node) {
      switch (node.getType()) {
        case TYPE_SERVICE_IMPLEMENTATION:
        case TYPE_SERVICE_INTERFACE:
        case TYPE_SERVICE_REGISTRATION:
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

    private void validateDropCopy(DndEvent dndEvent) {
      switch (dndEvent.node.getType()) {
        case TYPE_SERVICE_REGISTRATION:
          dndEvent.doit = dndEvent.targetParent.getType() == IScoutBundle.BUNDLE_CLIENT;
          break;
        default:
          dndEvent.doit = false;
          break;
      }
    }

    private void validateDropMove(DndEvent dndEvent) {
      switch (dndEvent.node.getType()) {
        case TYPE_SERVICE_REGISTRATION:
        case TYPE_SERVICE_IMPLEMENTATION:
        case TYPE_SERVICE_INTERFACE:
          dndEvent.doit = dndEvent.targetParent.getType() == IScoutBundle.BUNDLE_CLIENT;
          break;
        default:
          dndEvent.doit = false;
          break;
      }

    }

    @Override
    public void dndPerformed(DndEvent dndEvent) {
      m_serviceNewWizardPage.pingStateChanging();
    }
  } // end class P_TreeDndListener

  private class P_StatusRevalidator implements IStatusProvider {
    @Override
    public void validate(Object source, MultiStatus multiStatus) {
      multiStatus.add(getStatusTypeNames());
      multiStatus.add(getStatusServiceRegistration());
      multiStatus.add(getStatusService());
    }

    protected IStatus getStatusTypeNames() {
      // client
      IScoutBundle serviceImplementationBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
      if (serviceImplementationBundle != null) {
        ITreeNode serviceImplNode = m_locationWizardPage.getTreeNode(TYPE_SERVICE_IMPLEMENTATION, true, true);
        if (serviceImplNode != null) {
          String fqn = serviceImplementationBundle.getPackageName(IScoutBundle.CLIENT_PACKAGE_APPENDIX_SERVICES) + "." + serviceImplNode.getText();
          if (serviceImplementationBundle.findType(fqn) != null) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + serviceImplNode.getText() + "' " + Texts.get("AlreadyExists") + ".");
          }
        }
      }
      // client
      IScoutBundle serviceInterfaceBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
      if (serviceInterfaceBundle != null) {
        ITreeNode serviceInterfaceNode = m_locationWizardPage.getTreeNode(TYPE_SERVICE_INTERFACE, true, true);
        if (serviceInterfaceNode != null) {
          String fqn = serviceInterfaceBundle.getPackageName(IScoutBundle.CLIENT_PACKAGE_APPENDIX_SERVICES) + "." + serviceInterfaceNode.getText();
          if (serviceInterfaceBundle.findType(fqn) != null) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + serviceInterfaceNode.getText() + "' " + Texts.get("AlreadyExists") + ".");
          }
        }
      }
      return Status.OK_STATUS;
    }

    protected IStatus getStatusService() {
      // client
      IScoutBundle serviceImplementationBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
      if (serviceImplementationBundle != null) {
        // client
        IScoutBundle serviceInterfaceBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
        if (serviceInterfaceBundle != null) {
          if (!serviceImplementationBundle.isOnClasspath(serviceInterfaceBundle)) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_locationWizardPage.getTextOfNode(TYPE_SERVICE_INTERFACE), m_locationWizardPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION)));
          }
        }
      }
      return Status.OK_STATUS;
    }

    protected IStatus getStatusServiceRegistration() {
      // client bundle
      IScoutBundle serviceInterfaceBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
      ITreeNode[] registrationNodes = m_locationWizardPage.getTreeNodes(TYPE_SERVICE_REGISTRATION, true, true);
      for (ITreeNode serviceRegNode : registrationNodes) {
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

  } // end class P_StatusRevalidator

}
