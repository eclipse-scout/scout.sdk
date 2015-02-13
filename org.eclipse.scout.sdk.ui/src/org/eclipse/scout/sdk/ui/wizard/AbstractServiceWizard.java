/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.wizard;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdkCore;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.service.ServiceRegistrationDescription;
import org.eclipse.scout.sdk.ui.fields.bundletree.ICheckStateListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNodeFilter;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link AbstractServiceWizard}</h3>
 *
 * @author Matthias Villiger
 * @since 3.10.0 22.11.2013
 */
public abstract class AbstractServiceWizard extends AbstractWorkspaceWizard {
  public static final String TYPE_SERVICE_REG_SESSION = "svcRegSession";

  private static final String SETTING_DEFAULT_SESSIONS = "settingDefaultSessions";

  protected static String getDefaultSessionSettingKey(IScoutBundle parent) {
    return SETTING_DEFAULT_SESSIONS + "_" + parent.getSymbolicName().replace('.', '_');
  }

  protected IType getLastUsedDefaultSession(IScoutBundle parent) {
    String fqn = getDialogSettings().get(getDefaultSessionSettingKey(parent));
    if (StringUtility.hasText(fqn)) {
      IType candidate = TypeUtility.getType(fqn);
      if (TypeUtility.exists(candidate)) {
        return candidate;
      }
    }
    return null;
  }

  public abstract BundleTreeWizardPage getLocationsPage();

  protected List<ServiceRegistrationDescription> getCheckedServiceRegistrations(Set<ITreeNode> serviceRegNodes) {
    List<ServiceRegistrationDescription> result = new LinkedList<ServiceRegistrationDescription>();
    for (ITreeNode n : serviceRegNodes) {
      if (getLocationsPage().isNodeChecked(n)) {
        IScoutBundle b = BundleTreeWizardPage.getLocationBundle(n);
        IType sessionToUse = null;
        for (ITreeNode child : n.getChildren(NodeFilters.getVisible())) {
          if (getLocationsPage().isNodeChecked(child)) {
            sessionToUse = (IType) child.getData();
            break;
          }
        }
        result.add(new ServiceRegistrationDescription(ScoutUtility.getJavaProject(b), sessionToUse));
      }
    }
    return result;
  }

  protected void storeUsedSession(ServiceRegistrationDescription sessionRegDesc) {
    if (sessionRegDesc.session != null) {
      IScoutBundle bundle = ScoutSdkCore.getScoutWorkspace().getBundleGraph().getBundle(sessionRegDesc.targetProject);
      if (bundle != null) {
        getDialogSettings().put(getDefaultSessionSettingKey(bundle), sessionRegDesc.session);
      }
    }
  }

  protected void refreshAvailableSessions(ITreeNode serviceRegistrationNode, ITreeNode serviceRegNodeOrig) {
    BundleTreeWizardPage locationPage = getLocationsPage();
    IScoutBundle bundle = BundleTreeWizardPage.getLocationBundle(serviceRegistrationNode);
    if (bundle == null) {
      return;
    }
    IType defaultSelection = null;
    // calculate which session should be selected by default
    if (locationPage != null) {
      // 1. if a session is selected in the node we are copying from: inherit this selection
      for (ITreeNode sibling : serviceRegNodeOrig.getChildren(NodeFilters.getVisible())) {
        if (locationPage.isNodeChecked(sibling)) {
          defaultSelection = (IType) sibling.getData();
          break;
        }
      }
      // 2. if nothing was inherited (see above): use the session used the last time.
      if (defaultSelection == null) {
        defaultSelection = getLastUsedDefaultSession(bundle);
      }
    }

    // rebuild nodes
    serviceRegistrationNode.clearChildren();
    boolean defaultFound = false;
    List<ITreeNode> createdNodes = null;
    Set<IType> sessions = ScoutTypeUtility.getSessionTypes(bundle.getJavaProject());
    if (sessions != null) {
      createdNodes = new ArrayList<ITreeNode>(sessions.size());
      int pos = 0;
      for (IType session : sessions) {
        boolean isServerSession = TypeUtility.getSupertypeHierarchy(session).contains(TypeUtility.getType(IRuntimeClasses.IServerSession));
        ImageDescriptor icon = null;
        if (isServerSession) {
          icon = ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ServerSession);
        }
        else {
          icon = ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ClientSession);
        }

        createdNodes.add(TreeUtility.createNode(serviceRegistrationNode, TYPE_SERVICE_REG_SESSION,
            Texts.get("UseSessionInRegistration", session.getElementName()), icon, pos++, session));

        // remember if the current default session could be found amongst all available sessions
        if (defaultSelection != null && defaultSelection.equals(session)) {
          defaultFound = true;
        }
      }
    }

    // set defaults
    if (locationPage != null) {
      if (defaultSelection == null || !defaultFound) {
        // fall back if we have no valid default session found
        defaultSelection = ScoutUtility.getNearestType(sessions, bundle);
      }

      // if we are copying a node: inherit the selection of the orig.
      if (serviceRegistrationNode != serviceRegNodeOrig) {
        locationPage.setNodeChecked(serviceRegistrationNode, locationPage.isNodeChecked(serviceRegNodeOrig));
      }

      // activate the default session.
      if (createdNodes != null && sessions != null) {
        int i = 0;
        for (IType session : sessions) {
          locationPage.setNodeChecked(createdNodes.get(i), CompareUtility.equals(session, defaultSelection));
          i++;
        }
      }

      // reload
      locationPage.refreshTree();
    }
  }

  protected class P_SessionCheckListener implements ICheckStateListener {
    public P_SessionCheckListener() {
    }

    @Override
    public void fireNodeCheckStateChanged(ITreeNode node, boolean checkState) {
      if (checkState && node.getType() == TYPE_SERVICE_REG_SESSION) {
        for (ITreeNode sibling : node.getParent().getChildren(NodeFilters.getVisible())) {
          if (sibling != node) {
            getLocationsPage().setNodeChecked(sibling, false);
          }
        }
      }
    }
  }

  protected class P_InitialCheckedFilter implements ITreeNodeFilter {
    public P_InitialCheckedFilter() {
    }

    @Override
    public boolean accept(ITreeNode node) {
      if (node.getType() == TYPE_SERVICE_REG_SESSION) {
        IScoutBundle b = BundleTreeWizardPage.getLocationBundle(node);
        IType curSession = (IType) node.getData();
        List<ITreeNode> siblings = node.getParent().getChildren(NodeFilters.getVisible());
        IType defaultSession = getLastUsedDefaultSession(b);
        boolean defaultSessionFound = false;
        Set<IType> sessions = new LinkedHashSet<IType>(siblings.size());
        for (int i = 0; i < siblings.size(); i++) {
          IType session = (IType) siblings.get(i).getData();
          sessions.add(session);
          if (!defaultSessionFound && session.equals(defaultSession)) {
            defaultSessionFound = true;
          }
        }
        if (defaultSession == null || !defaultSessionFound) {
          defaultSession = ScoutUtility.getNearestType(sessions, b);
        }
        return CompareUtility.equals(defaultSession, curSession);
      }
      return true;
    }
  } // end class P_InitialCheckedFilter
}
