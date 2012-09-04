/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.view.presenter;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.window.Window;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.resources.ResourcesContentProvider;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.scout.sdk.ws.jaxws.Texts;
import org.eclipse.scout.sdk.ws.jaxws.operation.SourceFolderCreateOrUpdateOperation;
import org.eclipse.scout.sdk.ws.jaxws.swt.dialog.ScoutWizardDialogEx;
import org.eclipse.scout.sdk.ws.jaxws.swt.view.proposal.PathProposal;
import org.eclipse.scout.sdk.ws.jaxws.swt.wizard.SourceFolderNewWizard;
import org.eclipse.swt.widgets.Composite;

/**
 * @deprecated use {@link ProposalTextField} with {@link ResourcesContentProvider}
 */
@Deprecated
public class SourceFolderPresenter extends ProposalPresenter<PathProposal> {

  public SourceFolderPresenter(Composite parent, PropertyViewFormToolkit toolkit) {
    super(parent, toolkit, false);
    setLabel(Texts.get("SourceFolder"));
    setTooltip(Texts.get("ClickToCreateNewSourceFolder"));
    setUseLinkAsLabel(true);
    callInitializer();
  }

  @Override
  protected void execLinkAction() throws CoreException {
    SourceFolderNewWizard wizard = new SourceFolderNewWizard(m_bundle);
    ScoutWizardDialogEx wizardDialog = new ScoutWizardDialogEx(wizard);
    if (wizardDialog.open() == Window.OK) {
      SourceFolderCreateOrUpdateOperation op = new SourceFolderCreateOrUpdateOperation();
      op.setBundle(m_bundle);
      op.setSourceFolder(wizard.getSourceFolder());
      op.run(new NullProgressMonitor(), null);

      IPath sourceFolder = new Path(wizard.getSourceFolder().lastSegment());
      setInputInternal(new PathProposal(sourceFolder));
      setValueFromUI(new PathProposal(sourceFolder));
    }
  }

  @Override
  protected void setInputInternal(PathProposal input) {
    setStateChanging(true);
    try {
      setProposals(createSourceFolderProposals());
    }
    finally {
      setStateChanging(false);
    }
    super.setInputInternal(input);
  }

  private PathProposal[] createSourceFolderProposals() {
    List<PathProposal> proposals = new LinkedList<PathProposal>();
    try {
      for (IClasspathEntry classpathEntry : m_bundle.getJavaProject().getRawClasspath()) {
        if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
          proposals.add(new PathProposal(new Path(classpathEntry.getPath().lastSegment())));
        }
      }
    }
    catch (JavaModelException e) {
      JaxWsSdk.logError("Error occured while fetching source folders.", e);
    }

    return proposals.toArray(new PathProposal[proposals.size()]);
  }
}
