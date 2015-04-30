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
package org.eclipse.scout.sdk.ui.wizard.page;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.page.PageNewOperation;
import org.eclipse.scout.sdk.ui.fields.bundletree.DndEvent;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeDndListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNodeFilter;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.ui.wizard.BundleTreeWizardPage;
import org.eclipse.scout.sdk.ui.wizard.IStatusProvider;
import org.eclipse.scout.sdk.util.signature.SignatureCache;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutBundleFilter;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.dnd.DND;
import org.eclipse.ui.IWorkbench;

public class PageNewWizard extends AbstractWorkspaceWizard {

  private static final String TYPE_PAGE = "page";
  private static final String TYPE_PAGE_DATA = "pageData";

  // members
  private IScoutBundle m_clientBundle;
  private ITreeNode m_locationWizardPageRoot;
  private PageNewOperation m_operation;

  // pages
  private PageNewTemplatesWizardPage m_templatePage;
  private PageNewAttributesWizardPage m_pageAttributePage;
  private BundleTreeWizardPage m_locationWizardPage;

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    setWindowTitle(Texts.get("NewPage"));

    m_clientBundle = UiUtility.getScoutBundleFromSelection(selection, ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT));
    IType holderType = UiUtility.getTypeFromSelection(selection);

    m_templatePage = new PageNewTemplatesWizardPage(m_clientBundle);
    addPage(m_templatePage);

    if (m_clientBundle != null) {
      m_locationWizardPageRoot = createTree(m_clientBundle);

      P_StatusRevalidator validator = new P_StatusRevalidator();
      m_locationWizardPage = new BundleTreeWizardPage(Texts.get("LookupServiceLocation"), Texts.get("OrganiseLocations"), m_locationWizardPageRoot, new P_InitialCheckedFilter());

      m_pageAttributePage = new PageNewAttributesWizardPage(m_clientBundle);
      m_pageAttributePage.addPropertyChangeListener(new P_PageAttributesPropertyListener());
      m_pageAttributePage.addStatusProvider(validator);
      m_pageAttributePage.setHolderType(holderType); // parent page or outline
      addPage(m_pageAttributePage);

      m_locationWizardPage.addStatusProvider(validator);
      m_locationWizardPage.addDndListener(new P_TreeDndListener());
      addPage(m_locationWizardPage);
      m_locationWizardPage.setExcludePage(true);

      String pck = UiUtility.getPackageSuffix(selection);
      if (StringUtility.hasText(pck)) {
        m_pageAttributePage.setTargetPackage(pck);
      }
    }
  }

  public void setLocationWizardPageVisible(boolean visible) {
    m_locationWizardPage.setExcludePage(!visible);
  }

  private static ITreeNode createTree(IScoutBundle clientBundle) {
    IScoutBundle sharedBundle = clientBundle.getParentBundle(ScoutBundleFilters.getMultiFilterAnd(ScoutBundleFilters.getWorkspaceBundlesFilter(), ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED)), false);
    IScoutBundleFilter bundleFilter = ScoutBundleFilters.getMultiFilterAnd(ScoutBundleFilters.getWorkspaceBundlesFilter(), ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT, IScoutBundle.TYPE_SHARED));
    ITreeNode rootNode = TreeUtility.createBundleTree(clientBundle, bundleFilter);

    ITreeNode clientNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(clientBundle));

    // page
    TreeUtility.createNode(clientNode, TYPE_PAGE, Texts.get("Page"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class));

    if (sharedBundle != null) {
      ITreeNode sharedNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(sharedBundle));
      // page data
      TreeUtility.createNode(sharedNode, TYPE_PAGE_DATA, Texts.get("PageData"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class));
    }

    return rootNode;
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    IScoutBundle pageDataProject = m_locationWizardPage.getLocationBundle(TYPE_PAGE_DATA, true, true);
    String pageDataPackage = null;
    IJavaProject pageDataJavaProject = null;
    if (pageDataProject != null) {
      pageDataJavaProject = pageDataProject.getJavaProject();
      pageDataPackage = pageDataProject.getPackageName(m_pageAttributePage.getTargetPackage());
    }
    m_operation = new PageNewOperation(m_pageAttributePage.getTypeName(), m_clientBundle.getPackageName(m_pageAttributePage.getTargetPackage()),
        pageDataPackage, m_clientBundle.getJavaProject(), pageDataJavaProject);
    m_operation.setNlsEntry(m_pageAttributePage.getNlsName());

    IType superType = m_pageAttributePage.getSuperType();
    if (TypeUtility.exists(superType)) {
      m_operation.setSuperTypeSignature(SignatureCache.createTypeSignature(superType.getFullyQualifiedName()));
    }
    m_operation.setHolderType(m_pageAttributePage.getHolderType());
    m_operation.setFormatSource(true);

    return true;
  }

  @Override
  protected boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    m_operation.validate();
    m_operation.run(monitor, workingCopyManager);
    return true;
  }

  private final class P_PageAttributesPropertyListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (PageNewAttributesWizardPage.PROP_TYPE_NAME.equals(evt.getPropertyName())) {
        String typeName = m_pageAttributePage.getTypeName();
        if (StringUtility.isNullOrEmpty(typeName)) {
          TreeUtility.findNode(m_locationWizardPageRoot, NodeFilters.getByType(TYPE_PAGE)).setText(Texts.get("Page"));
          TreeUtility.findNode(m_locationWizardPageRoot, NodeFilters.getByType(TYPE_PAGE_DATA)).setText(Texts.get("PageData"));
        }
        else {
          TreeUtility.findNode(m_locationWizardPageRoot, NodeFilters.getByType(TYPE_PAGE)).setText(typeName);
          TreeUtility.findNode(m_locationWizardPageRoot, NodeFilters.getByType(TYPE_PAGE_DATA)).setText(typeName + "Data");
        }
        m_locationWizardPage.refreshTree();
      }
    }
  }

  private static final class P_TreeDndListener implements ITreeDndListener {
    @Override
    public boolean isDragableNode(ITreeNode node) {
      return TYPE_PAGE_DATA.equals(node.getType());
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
        dndEvent.doit = false;
        return;
      }
      if (dndEvent.operation == DND.DROP_MOVE) {
        if (TYPE_PAGE_DATA.equals(dndEvent.node.getType())) {
          dndEvent.doit = IScoutBundle.TYPE_SHARED.equals(dndEvent.targetParent.getType());
        }
        else {
          dndEvent.doit = false;
        }
      }
    }

    @Override
    public void dndPerformed(DndEvent dndEvent) {
    }
  }

  private final class P_StatusRevalidator implements IStatusProvider {

    @Override
    public void validate(Object source, MultiStatus multiStatus) {
      multiStatus.add(getStatusPageData(source));
    }

    private IStatus getStatusPageData(Object source) {
      IScoutBundle pageDataBundle = m_locationWizardPage.getLocationBundle(TYPE_PAGE_DATA, true, true);
      if (pageDataBundle != null) {
        // check if the page data class already exists
        ITreeNode pageDataNode = m_locationWizardPage.getTreeNode(TYPE_PAGE_DATA, true, true);
        if (pageDataNode != null) {
          try {
            String fqn = pageDataBundle.getPackageName(m_pageAttributePage.getTargetPackage()) + "." + pageDataNode.getText();
            if (pageDataBundle.getJavaProject().findType(fqn) != null) {
              return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, "'" + pageDataNode.getText() + "' " + Texts.get("AlreadyExists") + ".");
            }
          }
          catch (JavaModelException e) {
            ScoutSdkUi.logError(e);
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("AnErrorOccured"));
          }
        }

        // check if the page data is on the class path of the page
        if (!ScoutTypeUtility.isOnClasspath(pageDataBundle, m_clientBundle)) {
          return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_locationWizardPage.getTextOfNode(TYPE_PAGE_DATA), m_locationWizardPage.getTextOfNode(TYPE_PAGE)));
        }
      }
      return Status.OK_STATUS;
    }
  }

  private static final class P_InitialCheckedFilter implements ITreeNodeFilter {
    @Override
    public boolean accept(ITreeNode node) {
      return TreeUtility.isOneOf(node.getType(), TYPE_PAGE, TYPE_PAGE_DATA);
    }
  } // end class P_InitialCheckedFilter
}
