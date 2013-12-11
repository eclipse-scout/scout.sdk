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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.targetpackage.IDefaultTargetPackage;
import org.eclipse.scout.sdk.operation.form.SearchFormNewOperation;
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
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.dnd.DND;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class SearchFormNewWizard extends AbstractWorkspaceWizard implements INewWizard {

  public static final String TYPE_SEARCH_FORM = "searchForm";
  public static final String TYPE_SEARCH_FORM_DATA = "searchFormData";
  public static final String TYPE_HANDLER_SEARCH = "searchHandler";

  private IScoutBundle m_clientBundle;
  private SearchFormNewWizardPage m_page1;
  private BundleTreeWizardPage m_page2;
  private SearchFormNewOperation m_operation;
  private ITreeNode m_locationPageRoot;

  public SearchFormNewWizard() {
    this(null);
  }

  public SearchFormNewWizard(IScoutBundle clientBundle) {
    setWindowTitle(Texts.get("NewSearchForm"));
    m_clientBundle = clientBundle;
  }

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    m_clientBundle = UiUtility.getScoutBundleFromSelection(selection, m_clientBundle, ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_CLIENT));
    String pck = UiUtility.getPackageSuffix(selection);

    P_StatusRevalidator statusProvider = new P_StatusRevalidator();
    m_page1 = new SearchFormNewWizardPage(m_clientBundle);
    m_page1.setTargetPackage(pck);
    m_page1.addStatusProvider(statusProvider);
    addPage(m_page1);

    if (m_clientBundle != null) {
      m_locationPageRoot = createTree(m_clientBundle);
      m_page2 = new BundleTreeWizardPage(Texts.get("SearchFormClassLocations"), Texts.get("OrganiseLocations"), m_locationPageRoot, new P_InitialCheckedFilter());
      m_page2.addStatusProvider(statusProvider);
      m_page2.addDndListener(new P_TreeDndListener());
      addPage(m_page2);

      // init
      m_page1.addPropertyChangeListener(new P_LocationPropertyListener());
    }
  }

  public void setTablePage(IType tablePage) {
    if (tablePage != null) {
      m_page1.setTablePageType(tablePage);
    }
  }

  public void setNlsEntry(INlsEntry nlsEntry) {
    if (nlsEntry != null) {
      m_page1.setNlsName(nlsEntry);
      m_page1.setTypeName(nlsEntry.getKey() + SdkProperties.SUFFIX_SEARCH_FORM);
    }
    else {
      m_page1.setNlsName(null);
    }
  }

  private ITreeNode createTree(IScoutBundle clientBundle) {
    IScoutBundle sharedBundle = clientBundle.getParentBundle(ScoutBundleFilters.getMultiFilterAnd(ScoutBundleFilters.getWorkspaceBundlesFilter(), ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED)), false);
    ITreeNode rootNode = TreeUtility.createBundleTree(clientBundle,
        NodeFilters.getByType(IScoutBundle.TYPE_CLIENT, IScoutBundle.TYPE_SHARED),
        ScoutBundleFilters.getWorkspaceBundlesFilter());
    ITreeNode clientNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(clientBundle));

    // form
    ITreeNode formNode = TreeUtility.createNode(clientNode, TYPE_SEARCH_FORM, Texts.get("SearchForm"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class));

    // searchHandler
    TreeUtility.createNode(formNode, TYPE_HANDLER_SEARCH, Texts.get("SearchHandler"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class));

    if (sharedBundle != null) {
      ITreeNode sharedNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(sharedBundle));
      // formData
      TreeUtility.createNode(sharedNode, TYPE_SEARCH_FORM_DATA, Texts.get("SearchFormData"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class));
    }
    return rootNode;
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    // fill operation before gui is disposed
    IScoutBundle searchFormBundle = m_page2.getLocationBundle(TYPE_SEARCH_FORM, true, true);
    m_operation = new SearchFormNewOperation(m_page1.getTypeName(), searchFormBundle.getPackageName(m_page1.getTargetPackage(IDefaultTargetPackage.CLIENT_SEARCHFORMS)), ScoutUtility.getJavaProject(searchFormBundle));
    m_operation.setNlsEntry(m_page1.getNlsName());
    IType superTypeProp = m_page1.getSuperType();
    if (superTypeProp != null) {
      m_operation.setSuperTypeSignature(SignatureCache.createTypeSignature(superTypeProp.getFullyQualifiedName()));
    }
    m_operation.setTablePage(m_page1.getTablePageType());
    m_operation.setCreateSearchHandler(m_page2.getTreeNode(TYPE_HANDLER_SEARCH, true, true) != null);

    IScoutBundle searchFormDataBundle = m_page2.getLocationBundle(TYPE_SEARCH_FORM_DATA, true, true);
    m_operation.setSearchFormDataLocationBundle(searchFormDataBundle);
    m_operation.setSearchFormDataPackageName(searchFormDataBundle.getPackageName(m_page1.getTargetPackage(IDefaultTargetPackage.SHARED_SERVICES)));
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

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }

  private class P_LocationPropertyListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals(SearchFormNewWizardPage.PROP_TYPE_NAME)) {
        String typeName = m_page1.getTypeName();
        if (!StringUtility.isNullOrEmpty(typeName)) {
          String prefix = typeName.replaceAll(SdkProperties.SUFFIX_SEARCH_FORM + "$", "");
          TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SEARCH_FORM)).setText(prefix + SdkProperties.SUFFIX_SEARCH_FORM);
          TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SEARCH_FORM_DATA)).setText(prefix + SdkProperties.SUFFIX_SEARCH_FORM_DATA);
          m_page2.refreshTree();
        }
        m_page2.pingStateChanging();
      }
    }
  } // end class P_LocationPropertyListener

  private static class P_InitialCheckedFilter implements ITreeNodeFilter {
    @Override
    public boolean accept(ITreeNode node) {
      return TreeUtility.isOneOf(node.getType(), TYPE_HANDLER_SEARCH, TYPE_SEARCH_FORM, TYPE_SEARCH_FORM_DATA);
    }
  } // end class P_InitialCheckedFilter

  private class P_TreeDndListener implements ITreeDndListener {
    @Override
    public boolean isDragableNode(ITreeNode node) {
      return TYPE_SEARCH_FORM.equals(node.getType());
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
      if (dndEvent.node.getType() == TYPE_SEARCH_FORM) {
        ITreeNode formDataNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SEARCH_FORM_DATA));
        if (formDataNode != null) {
          ITreeNode oldFomDataParent = formDataNode.getParent();
          IScoutBundle formBundle = (IScoutBundle) dndEvent.node.getParent().getData();
          IScoutBundle[] sharedBundles = formBundle.getParentBundles(ScoutBundleFilters.getBundlesOfTypeFilter(IScoutBundle.TYPE_SHARED), false);
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
      m_page1.pingStateChanging();
    }

    private void validateDropCopy(DndEvent dndEvent) {
      dndEvent.doit = false;
    }

    private void validateDropMove(DndEvent dndEvent) {
      if (TYPE_SEARCH_FORM.equals(dndEvent.node.getType())) {
        dndEvent.doit = IScoutBundle.TYPE_CLIENT.equals(dndEvent.targetParent.getType());
      }
      else {
        dndEvent.doit = false;
      }
    }
  } // end class P_TreeDndListener

  private class P_StatusRevalidator implements IStatusProvider {

    @Override
    public void validate(Object source, MultiStatus multiStatus) {
      multiStatus.add(getStatusSearchForm(source));
      multiStatus.add(getStatusTypeNames(source));
    }

    protected IStatus getStatusTypeNames(Object source) {
      if (m_page2 == null) {
        return Status.OK_STATUS;
      }
      IScoutBundle searchFormBundle = m_page2.getLocationBundle(TYPE_SEARCH_FORM, true, true);
      if (searchFormBundle != null) {
        ITreeNode searchFormNode = m_page2.getTreeNode(TYPE_SEARCH_FORM, true, true);
        if (searchFormNode != null) {
          try {
            String fqn = searchFormBundle.getPackageName(m_page1.getTargetPackage(IDefaultTargetPackage.CLIENT_SEARCHFORMS)) + "." + searchFormNode.getText();
            if (searchFormBundle.getJavaProject().findType(fqn) != null) {
              return new Status((source instanceof SearchFormNewWizardPage) ? (IStatus.ERROR) : (IStatus.WARNING),
                  ScoutSdkUi.PLUGIN_ID, "'" + searchFormNode.getText() + "' " + Texts.get("AlreadyExists") + ".");
            }
          }
          catch (JavaModelException e) {
            ScoutSdkUi.logError(e);
            return new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("AnErrorOccured"));
          }
        }
      }

      IScoutBundle formDataBundle = m_page2.getLocationBundle(TYPE_SEARCH_FORM_DATA, true, true);
      if (formDataBundle != null) {
        ITreeNode formDataNode = m_page2.getTreeNode(TYPE_SEARCH_FORM_DATA, true, true);
        if (formDataNode != null) {
          try {
            String fqn = formDataBundle.getPackageName(m_page1.getTargetPackage(IDefaultTargetPackage.SHARED_SERVICES)) + "." + formDataNode.getText();
            if (formDataBundle.getJavaProject().findType(fqn) != null) {
              return new Status((source instanceof SearchFormNewWizardPage) ? (IStatus.WARNING) : (IStatus.ERROR),
                  ScoutSdkUi.PLUGIN_ID, "'" + formDataNode.getText() + "' " + Texts.get("AlreadyExists") + ".");
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

    protected IStatus getStatusSearchForm(Object source) {
      if (m_page2 != null) {
        IScoutBundle formBundle = m_page2.getLocationBundle(TYPE_SEARCH_FORM, true, true);
        if (formBundle != null) {
          IScoutBundle formDataBundle = m_page2.getLocationBundle(TYPE_SEARCH_FORM_DATA, true, true);
          if (formDataBundle != null) {
            if (!ScoutTypeUtility.isOnClasspath(formDataBundle, formBundle)) {
              return new Status((source instanceof SearchFormNewWizardPage) ? (IStatus.WARNING) : (IStatus.ERROR),
                  ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_page2.getTextOfNode(TYPE_SEARCH_FORM_DATA), m_page2.getTextOfNode(TYPE_SEARCH_FORM)));
            }
          }
        }
      }
      return Status.OK_STATUS;
    }
  } // end class P_StatusRevalidator
}
