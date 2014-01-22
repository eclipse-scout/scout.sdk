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
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.dnd.DND;

public class SqlServiceNewWizard extends AbstractServiceWizard {
  public static final String TYPE_SERVICE_IMPLEMENTATION = "svcImpl";
  public static final String TYPE_SERVICE_REG_SERVER = "svcServerReg";

  private final ServiceNewWizardPage m_serviceNewWizardPage;
  private final BundleTreeWizardPage m_locationWizardPage;
  private final ITreeNode m_locationWizardPageRoot;
  private ServiceNewOperation m_operation;

  public SqlServiceNewWizard(IScoutBundle serverBundle) {
    setWindowTitle(Texts.get("NewSQLService"));
    IType serviceSuperType = RuntimeClasses.getSuperType(IRuntimeClasses.ISqlService, serverBundle.getJavaProject());
    P_StatusRevalidator statusProvider = new P_StatusRevalidator();

    m_locationWizardPageRoot = createTree(serverBundle);
    m_locationWizardPage = new BundleTreeWizardPage(Texts.get("SQLServiceLocation"), Texts.get("OrganiseLocations"), m_locationWizardPageRoot, new P_InitialCheckedFilter());
    m_locationWizardPage.addStatusProvider(statusProvider);
    m_locationWizardPage.addDndListener(new P_TreeDndListener());
    m_locationWizardPage.addCheckSelectionListener(new P_SessionCheckListener());

    m_serviceNewWizardPage = new ServiceNewWizardPage(Texts.get("NewSQLService"), Texts.get("CreateANewSQLService"),
        TypeUtility.getType(IRuntimeClasses.ISqlService), SdkProperties.SUFFIX_SQL_SERVICE, serverBundle, DefaultTargetPackage.get(serverBundle, IDefaultTargetPackage.SERVER_SERVICES_SQL));
    m_serviceNewWizardPage.addStatusProvider(statusProvider);
    m_serviceNewWizardPage.setSuperType(serviceSuperType);
    m_serviceNewWizardPage.addPropertyChangeListener(new P_LocationPropertyListener());

    addPage(m_serviceNewWizardPage);
    addPage(m_locationWizardPage);

    // init
    m_serviceNewWizardPage.setSuperType(serviceSuperType);
  }

  private ITreeNode createTree(IScoutBundle serverBundle) {
    ITreeNode rootNode = TreeUtility.createBundleTree(serverBundle,
        NodeFilters.getByType(IScoutBundle.TYPE_SHARED, IScoutBundle.TYPE_SERVER),
        ScoutBundleFilters.getWorkspaceBundlesFilter());

    ITreeNode serverNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(serverBundle));
    TreeUtility.createNode(serverNode, TYPE_SERVICE_IMPLEMENTATION, Texts.get("Service"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), 1);
    ITreeNode svcRegNode = TreeUtility.createNode(serverNode, TYPE_SERVICE_REG_SERVER, Texts.get("ServiceRegistration"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Public), 2);
    refreshAvailableSessions(svcRegNode, svcRegNode);
    return rootNode;
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    m_operation = new ServiceNewOperation(null, m_locationWizardPage.getTextOfNode(TYPE_SERVICE_IMPLEMENTATION, true, true));
    IType superType = m_serviceNewWizardPage.getSuperType();
    if (superType != null) {
      m_operation.setImplementationSuperTypeSignature(SignatureCache.createTypeSignature(superType.getFullyQualifiedName()));
    }
    IScoutBundle implementationBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
    if (implementationBundle != null) {
      m_operation.setImplementationProject(implementationBundle.getJavaProject());
      m_operation.setImplementationPackageName(implementationBundle.getPackageName(m_serviceNewWizardPage.getTargetPackage()));
    }
    for (ServiceRegistrationDescription desc : getCheckedServiceRegistrations(m_locationWizardPage.getTreeNodes(TYPE_SERVICE_REG_SERVER, true, true))) {
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
          String prefix = typeName.replaceAll(SdkProperties.SUFFIX_SQL_SERVICE + "$", "");
          TreeUtility.findNode(m_locationWizardPageRoot, NodeFilters.getByType(TYPE_SERVICE_IMPLEMENTATION)).setText(prefix + SdkProperties.SUFFIX_SQL_SERVICE);
          m_locationWizardPage.refreshTree();
        }
      }
      m_locationWizardPage.pingStateChanging();
    }
  } // end class P_LocationPropertyListener

  private class P_TreeDndListener implements ITreeDndListener {
    @Override
    public boolean isDragableNode(ITreeNode node) {
      return TreeUtility.isOneOf(node.getType(), TYPE_SERVICE_IMPLEMENTATION, TYPE_SERVICE_REG_SERVER);
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
      if (dndEvent.node.getType() == TYPE_SERVICE_REG_SERVER) {
        refreshAvailableSessions(dndEvent.newNode, dndEvent.node);
      }
      m_serviceNewWizardPage.pingStateChanging();
    }

    private void validateDropCopy(DndEvent dndEvent) {
      if (TYPE_SERVICE_REG_SERVER.equals(dndEvent.node.getType())) {
        dndEvent.doit = IScoutBundle.TYPE_SERVER.equals(dndEvent.targetParent.getType());
      }
      else {
        dndEvent.doit = false;
      }
    }

    private void validateDropMove(DndEvent dndEvent) {
      String t = dndEvent.node.getType();
      if (TYPE_SERVICE_IMPLEMENTATION.equals(t) || TYPE_SERVICE_REG_SERVER.equals(t)) {
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
      multiStatus.add(getStatusServiceRegistrationServer());
      multiStatus.add(getStatusTypeNames());
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
      return Status.OK_STATUS;
    }

    protected IStatus getStatusServiceRegistrationServer() {
      IScoutBundle serviceImplementationBundle = m_locationWizardPage.getLocationBundle(TYPE_SERVICE_IMPLEMENTATION, true, true);
      ITreeNode[] serviceRegistrationServerNodes = m_locationWizardPage.getTreeNodes(TYPE_SERVICE_REG_SERVER, true, true);
      for (ITreeNode serviceRegNode : serviceRegistrationServerNodes) {
        Object data = serviceRegNode.getParent().getData();
        if (data instanceof IScoutBundle) {
          IScoutBundle serviceRegistrationBundle = (IScoutBundle) data;
          if (serviceImplementationBundle != null) {
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
