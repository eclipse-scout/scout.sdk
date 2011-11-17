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
import org.eclipse.jdt.core.Signature;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.form.SearchFormNewOperation;
import org.eclipse.scout.sdk.ui.fields.bundletree.DndEvent;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeDndListener;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNode;
import org.eclipse.scout.sdk.ui.fields.bundletree.ITreeNodeFilter;
import org.eclipse.scout.sdk.ui.fields.bundletree.NodeFilters;
import org.eclipse.scout.sdk.ui.fields.bundletree.TreeUtility;
import org.eclipse.scout.sdk.ui.fields.proposal.ITypeProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.NlsProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.ScoutProposalUtility;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.ui.wizard.BundleTreeWizardPage;
import org.eclipse.scout.sdk.ui.wizard.IStatusProvider;
import org.eclipse.scout.sdk.ui.wizard.services.ServiceNewWizardPage;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.IScoutProject;
import org.eclipse.scout.sdk.workspace.ScoutBundleFilters;
import org.eclipse.swt.dnd.DND;

public class SearchFormNewWizard extends AbstractWorkspaceWizard {

  public static final int TYPE_SEARCH_FORM = 100;
  public static final int TYPE_SEARCH_FORM_DATA = 101;
  public static final int TYPE_HANDLER_SEARCH = 102;

  private final IScoutBundle m_clientBundle;
  private SearchFormNewWizardPage m_page1;
  private BundleTreeWizardPage m_page2;
  private SearchFormNewOperation m_operation = new SearchFormNewOperation();
  private ITreeNode m_locationPageRoot;

  public SearchFormNewWizard(IScoutBundle clientBundle) {
    setWindowTitle(Texts.get("NewSearchForm"));
    P_StatusRevalidator statusProvider = new P_StatusRevalidator();
    m_clientBundle = clientBundle;
    m_page1 = new SearchFormNewWizardPage(clientBundle);
    m_page1.addStatusProvider(statusProvider);
    addPage(m_page1);
    m_locationPageRoot = createTree(clientBundle);
    m_page2 = new BundleTreeWizardPage(Texts.get("SearchFormClassLocations"), Texts.get("OrganiseLocations"), m_locationPageRoot, new P_InitialCheckedFilter());
    m_page2.addStatusProvider(statusProvider);
    m_page2.addDndListener(new P_TreeDndListener());
    addPage(m_page2);
    // init
    m_page1.addPropertyChangeListener(new P_LocationPropertyListener());
  }

  public void setTablePage(IType tablePage) {
    if (tablePage != null) {
      m_page1.setTablePageType(ScoutProposalUtility.getScoutTypeProposalsFor(tablePage)[0]);
    }
  }

  public void setNlsEntry(INlsEntry nlsEntry) {
    if (nlsEntry != null) {
      m_page1.setNlsName(new NlsProposal(nlsEntry, nlsEntry.getProject().getDevelopmentLanguage()));
      m_page1.setTypeName(nlsEntry.getKey() + SdkProperties.SUFFIX_SEARCH_FORM);
    }
    else {
      m_page1.setNlsName(null);
    }
  }

  private ITreeNode createTree(IScoutBundle clientBundle) {
    IScoutBundle sharedBundle = null;
    IScoutProject scoutProject = clientBundle.getScoutProject();
    while (sharedBundle == null && scoutProject != null) {
      sharedBundle = scoutProject.getSharedBundle();
      scoutProject = scoutProject.getParentProject();
    }
    ITreeNode rootNode = TreeUtility.createBundleTree(clientBundle.getScoutProject(), NodeFilters.getAcceptAll());

    ITreeNode clientNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(clientBundle));
    // form
    ITreeNode formNode = TreeUtility.createNode(clientNode, TYPE_SEARCH_FORM, Texts.get("SearchForm"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), TYPE_SEARCH_FORM);
    // searchHandler
    TreeUtility.createNode(formNode, TYPE_HANDLER_SEARCH, Texts.get("SearchHandler"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), TYPE_HANDLER_SEARCH);

    if (sharedBundle != null) {
      ITreeNode sharedNode = TreeUtility.findNode(rootNode, NodeFilters.getByData(sharedBundle));
      // formData
      TreeUtility.createNode(sharedNode, TYPE_SEARCH_FORM_DATA, Texts.get("SearchFormData"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.Class), TYPE_SEARCH_FORM_DATA);
    }
    return rootNode;
  }

  @Override
  protected boolean beforeFinish() throws CoreException {
    // fill operation before gui is disposed
    m_operation.setTypeName(m_page1.getTypeName());
    NlsProposal nlsProp = m_page1.getNlsName();
    if (nlsProp != null) {
      m_operation.setNlsEntry(nlsProp.getNlsEntry());
    }
    ITypeProposal superTypeProp = m_page1.getSuperType();
    if (superTypeProp != null) {
      m_operation.setSuperTypeSignature(Signature.createTypeSignature(superTypeProp.getType().getFullyQualifiedName(), true));
    }
    ITypeProposal tablePageProp = m_page1.getTablePageType();
    if (tablePageProp != null) {
      m_operation.setTablePage(tablePageProp.getType());
    }
    m_operation.setCreateSearchHandler(m_page2.getTreeNode(TYPE_HANDLER_SEARCH, true, true) != null);
    m_operation.setSearchFormLocationBundle(m_page2.getLocationBundle(TYPE_SEARCH_FORM, true, true));
    m_operation.setSearchFormDataLocationBundle(m_page2.getLocationBundle(TYPE_SEARCH_FORM_DATA, true, true));
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
      if (evt.getPropertyName().equals(ServiceNewWizardPage.PROP_TYPE_NAME)) {
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

  private class P_InitialCheckedFilter implements ITreeNodeFilter {
    @Override
    public boolean accept(ITreeNode node) {
      switch (node.getType()) {
        case TYPE_HANDLER_SEARCH:
        case TYPE_SEARCH_FORM:
        case TYPE_SEARCH_FORM_DATA:
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
        case TYPE_SEARCH_FORM:
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
      if (dndEvent.node.getType() == TYPE_SEARCH_FORM) {
        ITreeNode formDataNode = TreeUtility.findNode(m_locationPageRoot, NodeFilters.getByType(TYPE_SEARCH_FORM_DATA));
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
      m_page1.pingStateChanging();
    }

    private void validateDropCopy(DndEvent dndEvent) {
      dndEvent.doit = false;
    }

    private void validateDropMove(DndEvent dndEvent) {
      switch (dndEvent.node.getType()) {
        case TYPE_SEARCH_FORM:
          dndEvent.doit = dndEvent.targetParent.getType() == IScoutBundle.BUNDLE_CLIENT;
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
      multiStatus.add(getStatusSearchForm(source));
      multiStatus.add(getStatusTypeNames(source));
    }

    protected IStatus getStatusTypeNames(Object source) {
      IScoutBundle searchFormBundle = m_page2.getLocationBundle(TYPE_SEARCH_FORM, true, true);
      if (searchFormBundle != null) {
        ITreeNode searchFormNode = m_page2.getTreeNode(TYPE_SEARCH_FORM, true, true);
        if (searchFormNode != null) {
          String fqn = searchFormBundle.getPackageName(IScoutBundle.CLIENT_PACKAGE_APPENDIX_UI_SEARCHFORMS) + "." + searchFormNode.getText();
          if (searchFormBundle.findType(fqn) != null) {
            return new Status((source instanceof SearchFormNewWizardPage) ? (IStatus.ERROR) : (IStatus.WARNING),
                ScoutSdkUi.PLUGIN_ID, "'" + searchFormNode.getText() + "' " + Texts.get("AlreadyExists") + ".");
          }
        }
      }

      IScoutBundle formDataBundle = m_page2.getLocationBundle(TYPE_SEARCH_FORM_DATA, true, true);
      if (formDataBundle != null) {
        ITreeNode formDataNode = m_page2.getTreeNode(TYPE_SEARCH_FORM_DATA, true, true);
        if (formDataNode != null) {
          String fqn = formDataBundle.getPackageName(IScoutBundle.SHARED_PACKAGE_APPENDIX_SERVICES_PROCESS) + "." + formDataNode.getText();
          if (formDataBundle.findType(fqn) != null) {
            return new Status((source instanceof SearchFormNewWizardPage) ? (IStatus.WARNING) : (IStatus.ERROR),
                ScoutSdkUi.PLUGIN_ID, "'" + formDataNode.getText() + "' " + Texts.get("AlreadyExists") + ".");
          }
        }
      }
      return Status.OK_STATUS;
    }

    protected IStatus getStatusSearchForm(Object source) {
      IScoutBundle formBundle = m_page2.getLocationBundle(TYPE_SEARCH_FORM, true, true);
      if (formBundle != null) {
        IScoutBundle formDataBundle = m_page2.getLocationBundle(TYPE_SEARCH_FORM_DATA, true, true);
        if (formDataBundle != null) {
          if (!formBundle.isOnClasspath(formDataBundle)) {
            return new Status((source instanceof SearchFormNewWizardPage) ? (IStatus.WARNING) : (IStatus.ERROR),
                ScoutSdkUi.PLUGIN_ID, Texts.get("XIsNotAClasspathOfY", m_page2.getTextOfNode(TYPE_SEARCH_FORM_DATA), m_page2.getTextOfNode(TYPE_SEARCH_FORM)));
          }
        }

      }
      return Status.OK_STATUS;
    }

  } // end class P_StatusRevalidator

}
