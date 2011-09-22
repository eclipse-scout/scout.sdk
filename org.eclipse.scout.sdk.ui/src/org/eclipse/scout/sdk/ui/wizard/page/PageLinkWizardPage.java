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

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.operation.page.LinkPageOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.DefaultProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ITypeProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ScoutProposalUtility;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.type.TypeComparators;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>PageNewWizardPage2</h3> ...
 */
public class PageLinkWizardPage extends AbstractWorkspaceWizardPage {

  private IType iPage = ScoutSdk.getType(RuntimeClasses.IPage);
  private IType iOutline = ScoutSdk.getType(RuntimeClasses.IOutline);

  private ITypeProposal m_holderType;
  private ITypeProposal m_pageType;

  private ProposalTextField m_holderTypeField;
  private ProposalTextField m_pageTypeField;

  // process members
  private LinkPageOperation m_operation;

  private boolean m_holderTypeEnabled = true;
  private boolean m_pageTypeFieldEnabled = true;

  private IScoutBundle m_clientBundle;

  public PageLinkWizardPage(IScoutBundle clientBundle) {
    super(PageLinkWizardPage.class.getName());
    m_clientBundle = clientBundle;
    setTitle(Texts.get("LinkPage"));
    setDefaultMessage(Texts.get("LinkPageToAPageHolder"));
    setOperation(new LinkPageOperation());
  }

  @Override
  protected void createContent(Composite parent) {
    IType[] pages = SdkTypeUtility.getClassesOnClasspath(iPage, getClientBundle().getJavaProject());
    m_pageTypeField = getFieldToolkit().createProposalField(parent, new DefaultProposalProvider(ScoutProposalUtility.getScoutTypeProposalsFor(pages)), Texts.get("Page"));
    m_pageTypeField.acceptProposal(getPageType());
    m_pageTypeField.setEnabled(isPageTypeFieldEnabled());
    m_pageTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setPageType((ITypeProposal) event.proposal);
        pingStateChanging();
      }
    });

    IType[] outlines = SdkTypeUtility.getClassesOnClasspath(iOutline, getClientBundle().getJavaProject());
    IType[] propTypes = new IType[pages.length + outlines.length];
    System.arraycopy(pages, 0, propTypes, 0, pages.length);
    System.arraycopy(outlines, 0, propTypes, pages.length, outlines.length);
    Arrays.sort(propTypes, TypeComparators.getTypeNameComparator());
    ITypeProposal[] proposals = ScoutProposalUtility.getScoutTypeProposalsFor(propTypes);

    m_holderTypeField = getFieldToolkit().createProposalField(parent, new DefaultProposalProvider(proposals), Texts.get("HolderPageOutline"));
    m_holderTypeField.acceptProposal(getHolderType());
    m_holderTypeField.setEnabled(isHolderTypeEnabled());
    m_holderTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_holderType = (ITypeProposal) event.proposal;
        pingStateChanging();
      }
    });

    // layout
    parent.setLayout(new GridLayout(1, true));
    m_pageTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_holderTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException {
    // write back members
    getOperation().setHolderType(getHolderType().getType());
    getOperation().setPage(getPageType().getType());
    getOperation().run(monitor, workingCopyManager);
    return true;
  }

  @Override
  protected void validatePage(MultiStatus multiStatus) {
    if (getHolderType() == null) {
      multiStatus.add(new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("HolderTypeIsMissing")));
    }
    else if (getPageType() == null) {
      multiStatus.add(new Status(IStatus.ERROR, ScoutSdkUi.PLUGIN_ID, Texts.get("PageTypeIsMissing")));
    }
    else {
      multiStatus.add(Status.OK_STATUS);
    }
  }

  public void setOperation(LinkPageOperation operation) {
    m_operation = operation;
  }

  public LinkPageOperation getOperation() {
    return m_operation;
  }

  public IScoutBundle getClientBundle() {
    return m_clientBundle;
  }

  public void setHolderType(ITypeProposal holderPage) {
    try {
      setStateChanging(true);
      m_holderType = holderPage;
      if (isControlCreated()) {
        m_holderTypeField.acceptProposal(holderPage);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public ITypeProposal getHolderType() {
    return m_holderType;
  }

  public void setHolderTypeEnabled(boolean hoderTypeEnabled) {
    if (isControlCreated()) {
      throw new IllegalStateException("control already created.");
    }
    m_holderTypeEnabled = hoderTypeEnabled;
  }

  public boolean isHolderTypeEnabled() {
    return m_holderTypeEnabled;
  }

  public void setPageType(ITypeProposal pageType) {
    try {
      setStateChanging(true);
      m_pageType = pageType;
      if (isControlCreated()) {
        m_pageTypeField.acceptProposal(pageType);
      }
    }
    finally {
      setStateChanging(false);
    }
  }

  public ITypeProposal getPageType() {
    return m_pageType;
  }

  public void setPageTypeFieldEnabled(boolean pageTypeFieldEnabled) {
    m_pageTypeFieldEnabled = pageTypeFieldEnabled;
  }

  public boolean isPageTypeFieldEnabled() {
    return m_pageTypeFieldEnabled;
  }

}
