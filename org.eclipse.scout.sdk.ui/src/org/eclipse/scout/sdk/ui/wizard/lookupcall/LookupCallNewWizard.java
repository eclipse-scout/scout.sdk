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
package org.eclipse.scout.sdk.ui.wizard.lookupcall;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.lookupcall.LookupCallNewOperation;
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
import org.eclipse.scout.sdk.ui.wizard.lookupcall.LookupCallNewWizardPage.LOOKUP_SERVICE_STRATEGY;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.swt.dnd.DND;

/**
 * <h3>LookupCallNewWizard</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 25.08.2010
 */
public class LookupCallNewWizard extends AbstractWorkspaceWizard {

  public static final int TYPE_LOOKUPCALL = 100;
  public static final int TYPE_SERVICE_INTERFACE = 107;
  public static final int TYPE_SERVICE_IMPLEMENTATION = 108;
  public static final int TYPE_SERVICE_REG_CLIENT = 109;
  public static final int TYPE_SERVICE_REG_SERVER = 110;

  private final IScoutBundle m_sharedBundle;
  private LookupCallNewWizardPage m_page1;
  private BundleTreeWizardPage m_page2;
  private LookupCallNewOperation m_operation;
  private ITreeNode m_locationPageRoot;

  public LookupCallNewWizard(IScoutBundle sharedBundle) {
    setWindowTitle(Texts.get("NewLookupCall"));
    P_StatusRevalidator statusProvider = new P_StatusRevalidator();
    m_sharedBundle = sharedBundle;
    IScoutBundle clientBundle = null;
    IScoutBundle serverBundle = null;
    IScoutProject scoutProject = sharedBundle.getScoutProject();

    while ((clientBundle == null || serverBundle == null) && scoutProject != null) {
      clientBundle = scoutProject.getClientBundle();
      serverBundle = scoutProject.getServerBundle();
      scoutProject = scoutProject.getParentProject();
    }

    m_page1 = new LookupCallNewWizardPage(getSharedBundle(), serverBundle);
    addPage(m_page1);
    m_locationPageRoot = createTree(clientBundle, sharedBundle, serverBundle);
    m_page2 = new BundleTreeWizardPage(Texts.get("LookupCallLocations"), Texts.get("OrganiseLocations"), m_locationPageRoot, new P_InitialCheckedFilter());
    m_page2.addStatusProvider(statusProvider);
    m_page2.addDndListener(new P_TreeDndListener());
    addPage(m_page2);
    // init
    m_page1.addPropertyChangeListener(new P_LocationPropertyListener());

  }

  private ITreeNode createTree(IScoutBundle clientBundle, IScoutBundle sharedBundle, IScoutBundle serverBundle) {

    // BundleSet bundleSet=BundleLocationUtility.createBundleSet(clientBundle);
    ITreeNode rootNode = TreeUtility.createBundleTree(sharedBundle.getScoutProject(), NodeFilters.getAcceptAll());

    // client
    if (clientBundle != null) {
      ITreeNode clientNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(clientBundle));
      // service client reg
      ITreeNode clientRegNode = TreeUtility.createNode(clientNode, TYPE_SERVICE_REG_CLIENT, Texts.get("ServiceProxyRegistration"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Public), TYPE_SERVICE_REG_CLIENT);
      clientRegNode.setEnabled(false);
    }
    if (sharedBundle != null) {
      ITreeNode sharedNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(sharedBundle));
      // formData
      TreeUtility.createNode(sharedNode, TYPE_LOOKUPCALL, Texts.get("LookupCall"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), TYPE_LOOKUPCALL).setEnabled(false);
      if (serverBundle != null) {
        // service interface
        TreeUtility.createNode(sharedNode, TYPE_SERVICE_INTERFACE, Texts.get("IService"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Interface), TYPE_SERVICE_INTERFACE).setEnabled(false);
      }
    }
    if (serverBundle != null) {
      ITreeNode serverNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(serverBundle));
      // service implementation
      TreeUtility.createNode(serverNode, TYPE_SERVICE_IMPLEMENTATION, Texts.get("Service"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), TYPE_SERVICE_IMPLEMENTATION).setEnabled(false);
      TreeUtility.createNode(serverNode, TYPE_SERVICE_REG_SERVER, Texts.get("ServiceRegistration"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Public), TYPE_SERVICE_REG_SERVER).setEnabled(false);
    }

    return rootNode;
  }

  public LookupCallNewWizardPage getFormNewPage() {
    return m_page1;
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    // fill operation before gui is disposed
    m_operation = new LookupCallNewOperation();
    m_operation.setInterfaceRegistrationBundle(m_page2.getLocationBundle(TYPE_SERVICE_REG_CLIENT, true, true));
    m_operation.setBundle(m_page2.getLocationBundle(TYPE_LOOKUPCALL, true, true));
    m_operation.setFormatSource(false);
    m_operation.setImplementationRegistrationBundle(m_page2.getLocationBundle(TYPE_SERVICE_REG_SERVER, true, true));
    m_operation.setLookupCallName(m_page1.getTypeName());
    m_operation.setServiceImplementationBundle(m_page2.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true));
    m_operation.setServiceInterfaceBundle(m_page2.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true));
    switch (m_page1.getLookupServiceStrategy()) {
      case CREATE_NEW:
        IType superTypeProp = m_page1.getServiceSuperType();
        if (superTypeProp != null) {
          m_operation.setServiceSuperTypeSignature(SignatureCache.createTypeSignature(superTypeProp.getFullyQualifiedName()));
        }
        break;
      case USE_EXISTING:
        m_operation.setLookupService(m_page1.getLookupServiceType());
        break;
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

  public LookupCallNewWizardPage getPage1() {
    return m_page1;
  }

  public IScoutBundle getSharedBundle() {
    return m_sharedBundle;
  }

  private class P_LocationPropertyListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals(LookupCallNewWizardPage.PROP_TYPE_NAME)) {
        String typeName = m_page1.getTypeName();
        if (!StringUtility.isNullOrEmpty(typeName)) {
          String prefix = typeName.replaceAll(SdkProperties.SUFFIX_LOOKUP_CALL + "$", "");
          TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_LOOKUPCALL)).setText(prefix + SdkProperties.SUFFIX_LOOKUP_CALL);
          ITreeNode serviceImplNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SERVICE_IMPLEMENTATION));
          if (serviceImplNode != null) {
            serviceImplNode.setText(prefix + SdkProperties.SUFFIX_LOOKUP_SERVICE);
          }
          ITreeNode serviceInterfaceNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SERVICE_INTERFACE));
          if (serviceInterfaceNode != null) {
            serviceInterfaceNode.setText("I" + prefix + SdkProperties.SUFFIX_LOOKUP_SERVICE);
          }
          m_page2.refreshTree();
        }
        m_page2.pingStateChanging();
      }
      else if (evt.getPropertyName().equals(LookupCallNewWizardPage.PROP_LOOKUP_SERVICE_STRATEGY)) {
        LOOKUP_SERVICE_STRATEGY strategy = m_page1.getLookupServiceStrategy();
        switch (strategy) {
          case CREATE_NEW:
            TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SERVICE_IMPLEMENTATION)).setVisible(true);
            TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SERVICE_INTERFACE)).setVisible(true);
            TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SERVICE_REG_CLIENT)).setVisible(true);
            TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SERVICE_REG_SERVER)).setVisible(true);
            break;
          default:
            TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SERVICE_IMPLEMENTATION)).setVisible(false);
            TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SERVICE_INTERFACE)).setVisible(false);
            TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SERVICE_REG_CLIENT)).setVisible(false);
            TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SERVICE_REG_SERVER)).setVisible(false);
            break;
        }
        m_page2.refreshTree();
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
        case TYPE_LOOKUPCALL:
        case TYPE_SERVICE_IMPLEMENTATION:
        case TYPE_SERVICE_INTERFACE:
        case TYPE_SERVICE_REG_CLIENT:
        case TYPE_SERVICE_REG_SERVER:
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

      m_page1.pingStateChanging();
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
      multiStatus.add(getStatusLookupCall());
      multiStatus.add(getStatusService());
      multiStatus.add(getStatusServiceRegistrationClient());
      multiStatus.add(getStatusServiceRegistrationServer());
      multiStatus.add(getStatusTypeNames());
    }

    protected IStatus getStatusTypeNames() {
      IScoutBundle serviceImplementationBundle = m_page2.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
      if (serviceImplementationBundle != null) {
        ITreeNode serviceImplNode = m_page2.getTreeNode(TYPE_SERVICE_IMPLEMENTATION, true, true);
        if (serviceImplNode != null) {
          String fqn = serviceImplementationBundle.getPackageName(IScoutBundle.SERVER_PACKAGE_APPENDIX_SERVICES) + "." + serviceImplNode.getText();
          IType findType = serviceImplementationBundle.findType(fqn);
          if (findType != null && findType.exists()) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + serviceImplNode.getText() + "' " + Texts.get("AlreadyExists") + ".");
          }
        }
      }
      // shared bundles
      IScoutBundle serviceInterfaceBundle = m_page2.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
      if (serviceInterfaceBundle != null) {
        ITreeNode serviceInterfaceNode = m_page2.getTreeNode(TYPE_SERVICE_INTERFACE, true, true);
        if (serviceInterfaceNode != null) {
          String fqn = serviceInterfaceBundle.getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES) + "." + serviceInterfaceNode.getText();
          IType interfaceType = serviceInterfaceBundle.findType(fqn);
          if (interfaceType != null && interfaceType.exists()) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + serviceInterfaceNode.getText() + "' " + Texts.get("AlreadyExists") + ".");
          }
        }
      }
      return Status.OK_STATUS;
    }

    protected IStatus getStatusLookupCall() {
      IScoutBundle lookupCallBundle = m_page2.getLocationBundle(TYPE_LOOKUPCALL, true, true);
      if (lookupCallBundle != null) {
        IScoutBundle serviceInterfaceBundle = m_page2.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
        if (serviceInterfaceBundle != null) {
          if (!lookupCallBundle.isOnClasspath(serviceInterfaceBundle)) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_page2.getTextOfNode(TYPE_SERVICE_INTERFACE), m_page2.getTextOfNode(TYPE_LOOKUPCALL)));
          }
        }
      }
      return Status.OK_STATUS;
    }

    protected IStatus getStatusService() {
      IScoutBundle serviceImplementationBundle = m_page2.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
      if (serviceImplementationBundle != null) {
        IScoutBundle serviceInterfaceBundle = m_page2.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
        if (serviceInterfaceBundle != null) {
          if (!serviceImplementationBundle.isOnClasspath(serviceInterfaceBundle)) {
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_page2.getTextOfNode(TYPE_SERVICE_INTERFACE), m_page2.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION)));
          }
        }
      }
      return Status.OK_STATUS;
    }

    protected IStatus getStatusServiceRegistrationClient() {
      IScoutBundle serviceInterfaceBundle = m_page2.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
      ITreeNode[] serviceRegistrationClientNodes = m_page2.getTreeNodes(TYPE_SERVICE_REG_CLIENT, true, true);
      for (ITreeNode serviceRegNode : serviceRegistrationClientNodes) {
        Object data = serviceRegNode.getParent().getData();
        if (data instanceof IScoutBundle) {
          IScoutBundle serviceRegistrationBundle = (IScoutBundle) data;
          if (serviceInterfaceBundle != null && serviceRegistrationBundle != null) {
            if (!serviceRegistrationBundle.isOnClasspath(serviceInterfaceBundle)) {
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotOnClasspathOfServiceY", m_page2.getTextOfNode(TYPE_SERVICE_INTERFACE), serviceRegistrationBundle.getBundleName()));
            }
          }
        }
      }
      return Status.OK_STATUS;
    }

    protected IStatus getStatusServiceRegistrationServer() {
      IScoutBundle serviceImplementationBundle = m_page2.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
      ITreeNode[] serviceRegistrationServerNodes = m_page2.getTreeNodes(TYPE_SERVICE_REG_SERVER, true, true);
      for (ITreeNode serviceRegNode : serviceRegistrationServerNodes) {
        Object data = serviceRegNode.getParent().getData();
        if (data instanceof IScoutBundle) {
          IScoutBundle serviceRegistrationBundle = (IScoutBundle) data;
          if (serviceImplementationBundle != null && serviceRegistrationBundle != null) {
            if (!serviceRegistrationBundle.isOnClasspath(serviceImplementationBundle)) {
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotOnClasspathOfServiceY", m_page2.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION), serviceRegistrationBundle.getBundleName()));
            }
          }
        }
      }

      return Status.OK_STATUS;
    }

  } // end class P_StatusRevalidator

}
