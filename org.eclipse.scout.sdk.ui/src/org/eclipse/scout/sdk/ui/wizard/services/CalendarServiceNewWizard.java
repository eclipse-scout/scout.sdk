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
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutIdeProperties;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.operation.service.ServiceNewOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.bundletree.DndEvent;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeDndListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNodeFilter;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.fields.proposal.ITypeProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.ScoutProposalUtility;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.ui.wizard.BundleTreeWizardPage;
import org.eclipse.scout.sdk.ui.wizard.IStatusProvider;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.dnd.DND;

public class CalendarServiceNewWizard extends AbstractWorkspaceWizard {
  public static final int TYPE_SERVICE_INTERFACE = 107;
  public static final int TYPE_SERVICE_IMPLEMENTATION = 108;
  public static final int TYPE_SERVICE_REG_CLIENT = 109;
  public static final int TYPE_SERVICE_REG_SERVER = 110;

  private final IScoutBundle m_serverBundle;
  private ServiceNewWizardPage m_serviceNewWizardPage;
  private BundleTreeWizardPage m_locationWizardPage;
  private ITreeNode m_locationWizardPageRoot;
  private ServiceNewOperation m_operation = new ServiceNewOperation();

  public CalendarServiceNewWizard(IScoutBundle serverBundle) {
    P_StatusRevalidator statusProvider = new P_StatusRevalidator();
    m_serverBundle = serverBundle;
    m_serviceNewWizardPage = new ServiceNewWizardPage("New Calendar Service", "create a new calendar service.", ScoutSdk.getType(RuntimeClasses.ICalendarService), ScoutIdeProperties.SUFFIX_CALENDAR_SERVICE);
    m_serviceNewWizardPage.addStatusProvider(statusProvider);
    m_serviceNewWizardPage.addPropertyChangeListener(new P_LocationPropertyListener());
    addPage(m_serviceNewWizardPage);

    m_locationWizardPageRoot = createTree(serverBundle);
    m_locationWizardPage = new BundleTreeWizardPage("Calendar Service Location", "Use drag'n drop to organise the locations.\n Only selected items will be created.", m_locationWizardPageRoot, new P_InitialCheckerFilter());
    m_locationWizardPage.addStatusProvider(statusProvider);
    m_locationWizardPage.addDndListener(new P_TreeDndListener());
    addPage(m_locationWizardPage);

    // init
    m_serviceNewWizardPage.setSuperType(ScoutProposalUtility.getScoutTypeProposalsFor(ScoutSdk.getType(RuntimeClasses.AbstractService))[0]);
  }

  private ITreeNode createTree(IScoutBundle serverBundle) {

    // BundleSet bundleSet=BundleLocationUtility.createBundleSet(serverBundle);
    ITreeNode rootNode = TreeUtility.createBundleTree(serverBundle.getScoutProject(), NodeFilters.getAcceptAll());
    IScoutBundle sharedBundle = null;
    IScoutBundle clientBundle = null;
    IScoutProject scoutProject = serverBundle.getScoutProject();
    while ((sharedBundle == null || clientBundle == null) && scoutProject != null) {
      sharedBundle = scoutProject.getSharedBundle();
      clientBundle = scoutProject.getClientBundle();
      scoutProject = scoutProject.getParentProject();
    }
    if (clientBundle != null) {
      ITreeNode clientNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(clientBundle));
      // service client reg
      TreeUtility.createNode(clientNode, TYPE_SERVICE_REG_CLIENT, "Service Proxy Registration", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Public), TYPE_SERVICE_REG_CLIENT);
    }
    if (sharedBundle != null) {
      ITreeNode sharedNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(sharedBundle));
      // service interface
      TreeUtility.createNode(sharedNode, TYPE_SERVICE_INTERFACE, "IService", ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Interface), TYPE_SERVICE_INTERFACE);
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

  @Override
  protected boolean beforeFinish() throws CoreException {
    ITypeProposal superType = m_serviceNewWizardPage.getSuperType();
    if (superType != null) {
      m_operation.setServiceSuperTypeSignature(Signature.createTypeSignature(superType.getType().getFullyQualifiedName(), true));
    }
    // server bundle
    IScoutBundle implementationBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
    if (implementationBundle != null) {
      m_operation.setImplementationBundle(implementationBundle);
      m_operation.setServicePackageName(implementationBundle.getPackageName(IScoutBundle.SERVER_PACKAGE_APPENDIX_SERVICES_COMMON_CALENDAR));
      m_operation.setServiceName(m_locationWizardPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION, true, true));
    }
    // client bundles
    IScoutBundle[] regProxyLocations = m_locationWizardPage.getLocationBundles(TYPE_SERVICE_REG_CLIENT, true, true);
    for (IScoutBundle cb : regProxyLocations) {
      m_operation.addProxyRegistrationBundle(cb);
    }
    IScoutBundle[] serverRegBundles = m_locationWizardPage.getLocationBundles(TYPE_SERVICE_REG_SERVER, true, true);
    for (IScoutBundle sb : serverRegBundles) {
      m_operation.addServiceRegistrationBundle(sb);
    }
    IScoutBundle interfaceBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
    if (interfaceBundle != null) {
      m_operation.setInterfaceBundle(interfaceBundle);
      m_operation.setServiceInterfacePackageName(interfaceBundle.getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES_COMMON_CALENDAR));
    }
    m_operation.setServiceInterfaceName(m_locationWizardPage.getTextOfNode(TYPE_SERVICE_INTERFACE, true, true));
    m_operation.setServiceInterfaceSuperTypeSignature(Signature.createTypeSignature(RuntimeClasses.ICalendarService, true));
    return true;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) {
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
          String prefix = typeName.replaceAll(ScoutIdeProperties.SUFFIX_CALENDAR_SERVICE + "$", "");
          TreeUtility.findNode(m_locationWizardPageRoot, NodeFilters.getByType(TYPE_SERVICE_IMPLEMENTATION)).setText(prefix + ScoutIdeProperties.SUFFIX_CALENDAR_SERVICE);
          TreeUtility.findNode(m_locationWizardPageRoot, NodeFilters.getByType(TYPE_SERVICE_INTERFACE)).setText("I" + prefix + ScoutIdeProperties.SUFFIX_CALENDAR_SERVICE);
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
      multiStatus.add(getStatusService());
      multiStatus.add(getStatusServiceRegistrationClient());
      multiStatus.add(getStatusServiceRegistrationServer());
      multiStatus.add(getStatusTypeNames());
    }

    protected IStatus getStatusTypeNames() {

      IScoutBundle serviceImplementationBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
      if (serviceImplementationBundle != null) {
        ITreeNode serviceImplNode = m_locationWizardPage.getTreeNode(TYPE_SERVICE_IMPLEMENTATION, true, true);
        if (serviceImplNode != null) {
          String fqn = serviceImplementationBundle.getPackageName(IScoutBundle.SERVER_PACKAGE_APPENDIX_SERVICES_COMMON_CALENDAR) + "." + serviceImplNode.getText();
          if (serviceImplementationBundle.findType(fqn) != null) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + serviceImplNode.getText() + "' already exists.");
          }
        }
      }
      IScoutBundle serviceInterfaceBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
      if (serviceInterfaceBundle != null) {
        ITreeNode serviceInterfaceNode = m_locationWizardPage.getTreeNode(TYPE_SERVICE_INTERFACE, true, true);
        if (serviceInterfaceNode != null) {
          String fqn = serviceInterfaceBundle.getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES_COMMON_CALENDAR) + "." + serviceInterfaceNode.getText();
          if (serviceInterfaceBundle.findType(fqn) != null) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + serviceInterfaceNode.getText() + "' already exists.");
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
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + m_locationWizardPage.getTextOfNode(TYPE_SERVICE_INTERFACE) + " is not on classpath of '" + m_locationWizardPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION) + "'.");
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
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + m_locationWizardPage.getTextOfNode(TYPE_SERVICE_INTERFACE) + " is not on classpath of Service Registration in '" + serviceRegistrationBundle.getBundleName() + "'.");
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
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + m_locationWizardPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION) + " is not on classpath of Service Registration in '" + serviceRegistrationBundle.getBundleName() + "'.");
            }
          }
        }
      }

      return Status.OK_STATUS;
    }

  } // end class P_StatusRevalidator

}
