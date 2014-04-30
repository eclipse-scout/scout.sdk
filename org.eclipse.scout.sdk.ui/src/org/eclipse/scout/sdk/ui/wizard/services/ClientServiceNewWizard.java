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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.extensions.targetpackage.DefaultTargetPackage;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.operation.service.ServiceNewOperation;
import org.eclipse.scout.sdk.operation.service.ServiceRegistrationDescription;
import org.eclipse.scout.sdk.ui.fields.bundletree.DndEvent;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeDndListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractServiceWizard;
import org.eclipse.scout.sdk.ui.wizard.BundleTreeWizardPage;
import org.eclipse.scout.sdk.ui.wizard.IStatusProvider;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.dnd.DND;

public class ClientServiceNewWizard extends AbstractServiceWizard {

  public static final String TYPE_SERVICE_INTERFACE = "svcIfc";
  public static final String TYPE_SERVICE_IMPLEMENTATION = "svcImpl";
  public static final String TYPE_SERVICE_REGISTRATION = "svcReg";

  private final BundleTreeWizardPage m_locationWizardPage;
  private final ServiceNewWizardPage m_serviceNewWizardPage;
  private final ITreeNode m_locationPageRoot;
  private ServiceNewOperation m_operation;

  public ClientServiceNewWizard(IScoutBundle clientBundle) {
    setWindowTitle(Texts.get("NewClientService"));
    P_StatusRevalidator statusProvider = new P_StatusRevalidator();
    m_serviceNewWizardPage = new ServiceNewWizardPage(Texts.get("NewClientService"), Texts.get("CreateANewClientService"),
        TypeUtility.getType(IRuntimeClasses.IService), SdkProperties.SUFFIX_SERVICE, clientBundle, DefaultTargetPackage.get(clientBundle, IDefaultTargetPackage.CLIENT_SERVICES));
    m_serviceNewWizardPage.addStatusProvider(statusProvider);
    m_serviceNewWizardPage.addPropertyChangeListener(new P_LocationPropertyListener());
    addPage(m_serviceNewWizardPage);

    m_locationPageRoot = createTree(clientBundle);
    m_locationWizardPage = new BundleTreeWizardPage(Texts.get("ServiceLocation"), Texts.get("OrganiseLocations"), m_locationPageRoot, new P_InitialCheckedFilter());
    m_locationWizardPage.addStatusProvider(statusProvider);
    m_locationWizardPage.addDndListener(new P_TreeDndListener());
    m_locationWizardPage.addCheckSelectionListener(new P_SessionCheckListener());
    addPage(m_locationWizardPage);
    // init
    m_serviceNewWizardPage.setSuperType(RuntimeClasses.getSuperType(IRuntimeClasses.IService, clientBundle.getJavaProject()));
  }

  private ITreeNode createTree(IScoutBundle clientBundle) {
    ITreeNode rootNode = TreeUtility.createBundleTree(clientBundle,
        NodeFilters.getByType(IScoutBundle.TYPE_CLIENT, IScoutBundle.TYPE_SHARED),
        ScoutBundleFilters.getWorkspaceBundlesFilter());
    ITreeNode clientNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(clientBundle));
    // service client reg
    TreeUtility.createNode(clientNode, TYPE_SERVICE_IMPLEMENTATION, Texts.get("Service"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), 1);
    TreeUtility.createNode(clientNode, TYPE_SERVICE_INTERFACE, Texts.get("IService"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Interface), 2);
    ITreeNode svcRegNode = TreeUtility.createNode(clientNode, TYPE_SERVICE_REGISTRATION, Texts.get("ServiceRegistration"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Public), 3);
    //sessions
    refreshAvailableSessions(svcRegNode, svcRegNode);
    return rootNode;
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    m_operation = new ServiceNewOperation(m_locationWizardPage.getTextOfNode(TYPE_SERVICE_INTERFACE, true, true), m_locationWizardPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION, true, true));
    IScoutBundle implementationBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
    if (implementationBundle != null) {
      m_operation.setImplementationProject(implementationBundle.getJavaProject());
      m_operation.setImplementationPackageName(implementationBundle.getPackageName(m_serviceNewWizardPage.getTargetPackage()));
    }
    IScoutBundle interfaceBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
    if (interfaceBundle != null) {
      m_operation.setInterfaceProject(interfaceBundle.getJavaProject());
      m_operation.setInterfacePackageName(interfaceBundle.getPackageName(m_serviceNewWizardPage.getTargetPackage()));
    }
    IType superType = m_serviceNewWizardPage.getSuperType();
    if (superType != null) {
      m_operation.setImplementationSuperTypeSignature(SignatureCache.createTypeSignature(superType.getFullyQualifiedName()));
    }
    for (ServiceRegistrationDescription desc : getCheckedServiceRegistrations(m_locationWizardPage.getTreeNodes(TYPE_SERVICE_REGISTRATION, true, true))) {
      m_operation.addServiceRegistration(desc);
      storeUsedSession(desc);
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

  @Override
  public BundleTreeWizardPage getLocationsPage() {
    return m_locationWizardPage;
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
      }
      m_locationWizardPage.pingStateChanging();
    }
  } // end class P_LocationPropertyListener

  private class P_TreeDndListener implements ITreeDndListener {
    @Override
    public boolean isDragableNode(ITreeNode node) {
      return TreeUtility.isOneOf(node.getType(), TYPE_SERVICE_IMPLEMENTATION, TYPE_SERVICE_INTERFACE, TYPE_SERVICE_REGISTRATION);
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

    private void validateDropCopy(DndEvent dndEvent) {
      if (TYPE_SERVICE_REGISTRATION.equals(dndEvent.node.getType())) {
        dndEvent.doit = IScoutBundle.TYPE_CLIENT.equals(dndEvent.targetParent.getType());
      }
      else {
        dndEvent.doit = false;
      }
    }

    private void validateDropMove(DndEvent dndEvent) {
      String t = dndEvent.node.getType();
      if (TreeUtility.isOneOf(t, TYPE_SERVICE_REGISTRATION, TYPE_SERVICE_IMPLEMENTATION, TYPE_SERVICE_INTERFACE)) {
        dndEvent.doit = IScoutBundle.TYPE_CLIENT.equals(dndEvent.targetParent.getType());
      }
      else {
        dndEvent.doit = false;
      }
    }

    @Override
    public void dndPerformed(DndEvent dndEvent) {
      if (dndEvent.node.getType() == TYPE_SERVICE_REGISTRATION) {
        refreshAvailableSessions(dndEvent.newNode, dndEvent.node);
      }
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
      // client
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
      return Status.OK_STATUS;
    }

    protected IStatus getStatusService() {
      // client
      IScoutBundle serviceImplementationBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
      if (serviceImplementationBundle != null) {
        // client
        IScoutBundle serviceInterfaceBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
        if (serviceInterfaceBundle != null) {
          if (!ScoutTypeUtility.isOnClasspath(serviceInterfaceBundle, serviceImplementationBundle)) {
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
          if (serviceInterfaceBundle != null) {
            if (!ScoutTypeUtility.isOnClasspath(serviceInterfaceBundle, serviceRegistrationBundle)) {
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotOnClasspathOfServiceY", m_locationWizardPage.getTextOfNode(TYPE_SERVICE_INTERFACE), serviceRegistrationBundle.getSymbolicName()));
            }
          }
        }
      }
      return Status.OK_STATUS;
    }

  } // end class P_StatusRevalidator

}
