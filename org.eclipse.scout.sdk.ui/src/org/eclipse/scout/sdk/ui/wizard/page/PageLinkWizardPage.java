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

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.operation.page.LinkPageOperation;
import org.eclipse.scout.sdk.ui.fields.proposal.ContentProposalEvent;
import org.eclipse.scout.sdk.ui.fields.proposal.IProposalAdapterListener;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.AbstractJavaElementContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>PageNewWizardPage2</h3> ...
 */
public class PageLinkWizardPage extends AbstractWorkspaceWizardPage {

  private final IType iPage = TypeUtility.getType(IRuntimeClasses.IPage);
  private final IType iOutline = TypeUtility.getType(IRuntimeClasses.IOutline);

  private IType m_holderType;
  private IType m_pageType;

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
    setDescription(Texts.get("LinkPageToAPageHolder"));
    setOperation(new LinkPageOperation());
  }

  @Override
  protected void createContent(Composite parent) {
    m_pageTypeField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("Page"), new AbstractJavaElementContentProvider() {
      @Override
      protected Object[][] computeProposals() {
        Set<IType> pages = TypeUtility.getClassesOnClasspath(iPage, getClientBundle().getJavaProject(), null);
        return new Object[][]{pages.toArray(new IType[pages.size()])};
      }
    });
    m_pageTypeField.acceptProposal(getPageType());
    m_pageTypeField.setEnabled(isPageTypeFieldEnabled());
    m_pageTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        setPageType((IType) event.proposal);
        pingStateChanging();
      }
    });

    m_holderTypeField = getFieldToolkit().createJavaElementProposalField(parent, Texts.get("HolderPageOutline"), new AbstractJavaElementContentProvider() {
      @Override
      protected Object[][] computeProposals() {
        Set<IType> types = TypeUtility.getClassesOnClasspath(iPage, getClientBundle().getJavaProject(), null);
        types.addAll(TypeUtility.getClassesOnClasspath(iOutline, getClientBundle().getJavaProject(), null));
        return new Object[][]{types.toArray(new IType[types.size()])};
      }
    });
    m_holderTypeField.acceptProposal(getHolderType());
    m_holderTypeField.setEnabled(isHolderTypeEnabled());
    m_holderTypeField.addProposalAdapterListener(new IProposalAdapterListener() {
      @Override
      public void proposalAccepted(ContentProposalEvent event) {
        m_holderType = (IType) event.proposal;
        pingStateChanging();
      }
    });

    // layout
    parent.setLayout(new GridLayout(1, true));
    m_pageTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    m_holderTypeField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
  }

  @Override
  public boolean performFinish(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    // write back members
    getOperation().setHolderType(getHolderType());
    getOperation().setPage(getPageType());
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

  public void setHolderType(IType holderPage) {
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

  public IType getHolderType() {
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

  public void setPageType(IType pageType) {
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

  public IType getPageType() {
    return m_pageType;
  }

  public void setPageTypeFieldEnabled(boolean pageTypeFieldEnabled) {
    m_pageTypeFieldEnabled = pageTypeFieldEnabled;
  }

  public boolean isPageTypeFieldEnabled() {
    return m_pageTypeFieldEnabled;
  }

}
