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
import org.eclipse.jdt.core.JavaModelException;
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
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.dnd.DND;

/**
 * <h3>LookupCallNewWizard</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 25.08.2010
 */
public class LookupCallNewWizard extends AbstractWorkspaceWizard {

  public static final String TYPE_LOOKUPCALL = "lookupCall";
  public static final String TYPE_SERVICE_INTERFACE = "svcIfc";
  public static final String TYPE_SERVICE_IMPLEMENTATION = "svcImpl";
  public static final String TYPE_SERVICE_REG_CLIENT = "svcRegClient";
  public static final String TYPE_SERVICE_REG_SERVER = "svcRegServer";

  private final IScoutBundle m_sharedBundle;
  private LookupCallNewWizardPage m_page1;
  private BundleTreeWizardPage m_page2;
  private LookupCallNewOperation m_operation;
  private ITreeNode m_locationPageRoot;

  public LookupCallNewWizard(IScoutBundle sharedBundle) {
    setWindowTitle(Texts.get("NewLookupCall"));
    P_StatusRevalidator statusProvider = new P_StatusRevalidator();
    m_sharedBundle = sharedBundle;

    IScoutBundle clientBundle = sharedBundle.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT), false);
    IScoutBundle serverBundle = sharedBundle.getChildBundle(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SERVER), false);

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
    ITreeNode rootNode = TreeUtility.createBundleTree(sharedBundle, NodeFilters.getByType(IScoutBundle.TYPE_CLIENT, IScoutBundle.TYPE_SERVER, IScoutBundle.TYPE_SHARED));

    // client
    if (clientBundle != null) {
      ITreeNode clientNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(clientBundle));
      if (serverBundle != null) {
        // service client reg
        ITreeNode clientRegNode = TreeUtility.createNode(clientNode, TYPE_SERVICE_REG_CLIENT, Texts.get("ServiceProxyRegistration"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Public));
        clientRegNode.setEnabled(false);
      }
    }
    if (sharedBundle != null) {
      ITreeNode sharedNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(sharedBundle));
      // formData
      TreeUtility.createNode(sharedNode, TYPE_LOOKUPCALL, Texts.get("LookupCall"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), 1).setEnabled(false);
      if (serverBundle != null) {
        // service interface
        TreeUtility.createNode(sharedNode, TYPE_SERVICE_INTERFACE, Texts.get("IService"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Interface), 2).setEnabled(false);
      }
    }
    if (serverBundle != null) {
      ITreeNode serverNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(serverBundle));
      // service implementation
      TreeUtility.createNode(serverNode, TYPE_SERVICE_IMPLEMENTATION, Texts.get("Service"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), 1).setEnabled(false);
      TreeUtility.createNode(serverNode, TYPE_SERVICE_REG_SERVER, Texts.get("ServiceRegistration"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Public), 2).setEnabled(false);
    }

    return rootNode;
  }

  public LookupCallNewWizardPage getFormNewPage() {
    return m_page1;
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    // fill operation before gui is disposed
    IScoutBundle lookupCallBundle = m_page2.getLocationBundle(TYPE_LOOKUPCALL, true, true);
    m_operation = new LookupCallNewOperation(m_page1.getTypeName(), lookupCallBundle.getPackageName(m_page1.getTargetPackage()), ScoutUtility.getJavaProject(lookupCallBundle));
    IScoutBundle serviceProxyRegBundle = m_page2.getLocationBundle(TYPE_SERVICE_REG_CLIENT, true, true);
    if (serviceProxyRegBundle != null) {
      m_operation.setServiceProxyRegistrationProject(serviceProxyRegBundle.getJavaProject());
    }

    m_operation.setFormatSource(false);
    IScoutBundle serviceRegistrationBundle = m_page2.getLocationBundle(TYPE_SERVICE_REG_SERVER, true, true);
    if (serviceRegistrationBundle != null) {
      m_operation.setServiceRegistrationProject(serviceRegistrationBundle.getJavaProject());
    }

    IScoutBundle serviceBundle = m_page2.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
    if (serviceBundle != null) {
      m_operation.setServiceImplementationProject(serviceBundle.getJavaProject());
      m_operation.setServiceImplementationPackage(serviceBundle.getPackageName(m_page1.getTargetPackage()));
    }

    IScoutBundle interfaceBundle = m_page2.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
    if (interfaceBundle != null) {
      m_operation.setServiceInterfaceProject(interfaceBundle.getJavaProject());
      m_operation.setServiceInterfacePackageName(interfaceBundle.getPackageName(m_page1.getTargetPackage()));
    }
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
      m_page2.pingStateChanging();
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
      return !TYPE_LOOKUPCALL.equals(node.getType());
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
      m_page1.pingStateChanging();
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
          try {
            String fqn = serviceImplementationBundle.getPackageName(m_page1.getTargetPackage()) + "." + serviceImplNode.getText();
            IType findType = serviceImplementationBundle.getJavaProject().findType(fqn);
            if (findType != null && findType.exists()) {
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
      IScoutBundle serviceInterfaceBundle = m_page2.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
      if (serviceInterfaceBundle != null) {
        ITreeNode serviceInterfaceNode = m_page2.getTreeNode(TYPE_SERVICE_INTERFACE, true, true);
        if (serviceInterfaceNode != null) {
          try {
            String fqn = serviceInterfaceBundle.getPackageName(m_page1.getTargetPackage()) + "." + serviceInterfaceNode.getText();
            IType interfaceType = serviceInterfaceBundle.getJavaProject().findType(fqn);
            if (interfaceType != null && interfaceType.exists()) {
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

    protected IStatus getStatusLookupCall() {
      IScoutBundle lookupCallBundle = m_page2.getLocationBundle(TYPE_LOOKUPCALL, true, true);
      if (lookupCallBundle != null) {
        IScoutBundle serviceInterfaceBundle = m_page2.getLocationBundle(TYPE_SERVICE_INTERFACE, true, true);
        if (serviceInterfaceBundle != null) {
          if (!ScoutTypeUtility.isOnClasspath(serviceInterfaceBundle, lookupCallBundle)) {
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
          if (!ScoutTypeUtility.isOnClasspath(serviceInterfaceBundle, serviceImplementationBundle)) {
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
          if (serviceInterfaceBundle != null) {
            if (!ScoutTypeUtility.isOnClasspath(serviceInterfaceBundle, serviceRegistrationBundle)) {
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotOnClasspathOfServiceY", m_page2.getTextOfNode(TYPE_SERVICE_INTERFACE), serviceRegistrationBundle.getSymbolicName()));
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
          if (serviceImplementationBundle != null) {
            if (!ScoutTypeUtility.isOnClasspath(serviceImplementationBundle, serviceRegistrationBundle)) {
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotOnClasspathOfServiceY", m_page2.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION), serviceRegistrationBundle.getSymbolicName()));
            }
          }
        }
      }
      return Status.OK_STATUS;
    }
  } // end class P_StatusRevalidator
}
