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
package org.eclipse.scout.sdk.ui.wizard;

import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.IContentProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.NlsProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.SignatureProposalProvider;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.widgets.Composite;

public class WizardPageFieldToolkit {

  public NlsProposalTextField createNlsProposalTextField(Composite parent, INlsProject nlsProject, String label) {
    NlsProposalTextField field = new NlsProposalTextField(parent, nlsProject);
    field.setLabelText(label);
    return field;
  }

  public StyledTextField createStyledTextField(Composite parent, String label) {
    StyledTextField field = new StyledTextField(parent, label);
    return field;
  }

  public ProposalTextField createProposalField(Composite parent, IContentProposalProvider contentProposalProvider, String label) {
    ProposalTextField field = new ProposalTextField(parent, contentProposalProvider);
    field.setLabelText(label);
    return field;
  }

  public ProposalTextField createSignatureProposalField(Composite parent, IScoutBundle bundle, String label) {
    SignatureProposalProvider proposalProvider = new SignatureProposalProvider(bundle.getSearchScope(), false, false);
    return createProposalField(parent, proposalProvider, label);
  }

  // public ProposalTextField createBCTypeProposalField(Composite parent, IScoutProjectOld projectGroup, String label){
  // BcElementProposalProvider contentProvider=new BcElementProposalProvider(projectGroup, false, false);
  // return createProposalField(parent, contentProvider, label);
  // }

}
