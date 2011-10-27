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

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ui.fields.StyledTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.DefaultProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.IContentProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.NlsProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.ScoutProposalUtility;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.SignatureProposalProvider;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.scout.sdk.workspace.typecache.ITypeHierarchy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

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

  public Button createCheckboxField(Composite parent, String label) {
    Composite checkComposite = new Composite(parent, SWT.NONE);
    Label l = new Label(checkComposite, SWT.NONE);
    Button chk = new Button(checkComposite, SWT.CHECK);
    chk.setText(label);

    checkComposite.setLayout(new FormLayout());
    FormData labelData = new FormData();
    labelData.top = new FormAttachment(0, 0);
    labelData.left = new FormAttachment(0, 0);
    labelData.right = new FormAttachment(40, 0);
    labelData.bottom = new FormAttachment(100, 0);
    l.setLayoutData(labelData);

    FormData chkData = new FormData();
    chkData.top = new FormAttachment(0, 0);
    chkData.left = new FormAttachment(l, 5);
    chkData.right = new FormAttachment(100, 0);
    chkData.bottom = new FormAttachment(100, 0);
    chk.setLayoutData(chkData);
    checkComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
    return chk;
  }

  public ProposalTextField createSignatureProposalField(Composite parent, IScoutBundle bundle, String label) {
    SignatureProposalProvider proposalProvider = new SignatureProposalProvider(bundle.getSearchScope(), false, false);
    return createProposalField(parent, proposalProvider, label);
  }

  public ProposalTextField createFormFieldSiblingProposalField(Composite parent, IType declaringType) {
    ITypeHierarchy formFieldHierarchy = ScoutSdk.getLocalTypeHierarchy(declaringType);
    IType[] formFields = SdkTypeUtility.getFormFields(declaringType, formFieldHierarchy);
    SiblingProposal[] availableSiblings = ScoutProposalUtility.getSiblingProposals(formFields);
    ProposalTextField siblingField = createProposalField(parent, new DefaultProposalProvider(availableSiblings), "Sibling");
    siblingField.setEnabled(availableSiblings != null && availableSiblings.length > 1);
    SiblingProposal selectedProposal = SiblingProposal.SIBLING_END;
    IType firstButton = SdkTypeUtility.getFistProcessButton(declaringType, formFieldHierarchy);
    if (firstButton != null) {
      selectedProposal = new SiblingProposal(firstButton);
    }
    siblingField.acceptProposal(selectedProposal);
    return siblingField;
  }
  // public ProposalTextField createBCTypeProposalField(Composite parent, IScoutProjectOld projectGroup, String label){
  // BcElementProposalProvider contentProvider=new BcElementProposalProvider(projectGroup, false, false);
  // return createProposalField(parent, contentProvider, label);
  // }

}
