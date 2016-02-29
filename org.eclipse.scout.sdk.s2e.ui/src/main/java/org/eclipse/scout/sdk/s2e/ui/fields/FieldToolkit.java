/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.ui.fields;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.PackageContentProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.SourceFolderContentProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.StrictHierarchyTypeContentProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.TypeContentProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.text.StyledTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.text.TextField;
import org.eclipse.scout.sdk.s2e.util.S2eUtils.PublicAbstractPrimaryTypeFilter;
import org.eclipse.scout.sdk.s2e.util.S2eUtils.PublicPrimaryTypeFilter;
import org.eclipse.scout.sdk.s2e.util.ScoutTier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class FieldToolkit {

  public StyledTextField createStyledTextField(Composite parent, String label) {
    return new StyledTextField(parent, label);
  }

  public StyledTextField createStyledTextField(Composite parent, String label, int labelPercentage) {
    return new StyledTextField(parent, label, labelPercentage);
  }

  public ProposalTextField createProposalField(Composite parent, String label) {
    return createProposalField(parent, label, ProposalTextField.STYLE_INITIAL_SHOW_POPUP);
  }

  public ProposalTextField createProposalField(Composite parent, String label, int style) {
    return createProposalField(parent, label, style, TextField.DEFAULT_LABEL_PERCENTAGE);
  }

  public ProposalTextField createProposalField(Composite parent, String label, int style, int labelPercentage) {
    if (label == null) {
      label = "";
    }
    ProposalTextField field = new ProposalTextField(parent, style, labelPercentage);
    field.setLabelText(label);
    return field;
  }

  public ProposalTextField createPackageTextField(Composite parent, String label, IJavaProject project) {
    return createPackageTextField(parent, label, project, TextField.DEFAULT_LABEL_PERCENTAGE);
  }

  public ProposalTextField createPackageTextField(Composite parent, String label, IJavaProject project, int labelPercentage) {
    ProposalTextField proposalField = createProposalField(parent, label, ProposalTextField.STYLE_DEFAULT, labelPercentage);
    PackageContentProvider provider = new PackageContentProvider(project);
    proposalField.setContentProvider(provider);
    proposalField.setLabelProvider(provider);
    proposalField.setProposalDescriptionProvider(provider);
    return proposalField;
  }

  public ProposalTextField createSourceFolderTextField(Composite parent, String label, ScoutTier tier, int labelPercentage) {
    ProposalTextField proposalField = createProposalField(parent, label, ProposalTextField.STYLE_DEFAULT, labelPercentage);
    SourceFolderContentProvider provider = new SourceFolderContentProvider(tier);
    proposalField.setContentProvider(provider);
    proposalField.setLabelProvider(provider);
    return proposalField;
  }

  public ProposalTextField createAbstractTypeProposalField(Composite parent, String label, IJavaProject jp, String baseClassFqn) {
    return createAbstractTypeProposalField(parent, label, jp, baseClassFqn, TextField.DEFAULT_LABEL_PERCENTAGE);
  }

  public ProposalTextField createAbstractTypeProposalField(Composite parent, String label, IJavaProject jp, String baseClassFqn, int labelPercentage) {
    ProposalTextField proposalField = createProposalField(parent, label, ProposalTextField.STYLE_DEFAULT, labelPercentage);
    StrictHierarchyTypeContentProvider provider = new StrictHierarchyTypeContentProvider(jp, baseClassFqn);
    provider.setTypeProposalFilter(new PublicAbstractPrimaryTypeFilter());
    proposalField.setContentProvider(provider);
    proposalField.setLabelProvider(provider);
    proposalField.setProposalDescriptionProvider(provider);
    return proposalField;
  }

  public ProposalTextField createTypeProposalField(Composite parent, String label, IJavaProject jp) {
    return createTypeProposalField(parent, label, jp, TextField.DEFAULT_LABEL_PERCENTAGE);
  }

  public ProposalTextField createTypeProposalField(Composite parent, String label, IJavaProject jp, int labelPercentage) {
    ProposalTextField proposalField = createProposalField(parent, label, ProposalTextField.STYLE_DEFAULT, labelPercentage);
    TypeContentProvider provider = new TypeContentProvider(jp);
    provider.setTypeProposalFilter(new PublicPrimaryTypeFilter());

    proposalField.setContentProvider(provider);
    proposalField.setLabelProvider(provider);
    proposalField.setProposalDescriptionProvider(provider);
    return proposalField;
  }

  public Group createGroupBox(Composite parent, String label) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    group.setText(label);
    return group;
  }

  public Button createCheckBox(Composite parent, String label, boolean checkedByDefault) {
    Button btn = new Button(parent, SWT.CHECK);
    btn.setText(label);
    btn.setSelection(checkedByDefault);
    return btn;
  }
}
