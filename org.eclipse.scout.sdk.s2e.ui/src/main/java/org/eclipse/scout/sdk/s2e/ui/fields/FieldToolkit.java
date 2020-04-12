/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.fields;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.core.s.util.ScoutTier;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.JavaProjectContentProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.PackageContentProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.SourceFolderContentProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.StrictHierarchyTypeContentProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.TranslationStoreContentProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.proposal.content.TypeContentProvider;
import org.eclipse.scout.sdk.s2e.ui.fields.resource.ResourceTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.text.StyledTextField;
import org.eclipse.scout.sdk.s2e.ui.fields.text.TextField;
import org.eclipse.scout.sdk.s2e.ui.util.S2eUiUtils;
import org.eclipse.scout.sdk.s2e.util.JdtUtils.PublicAbstractPrimaryTypeFilter;
import org.eclipse.scout.sdk.s2e.util.JdtUtils.PublicPrimaryTypeFilter;
import org.eclipse.scout.sdk.s2e.util.S2eScoutTier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

public final class FieldToolkit {

  private FieldToolkit() {
  }

  public static StyledTextField createStyledTextField(Composite parent, String label, int type) {
    return createStyledTextField(parent, label, type, TextField.DEFAULT_LABEL_WIDTH);
  }

  public static StyledTextField createStyledTextField(Composite parent, String label, int type, int labelWidth) {
    StyledTextField styledTextField = new StyledTextField(parent, type, labelWidth);
    styledTextField.setLabelText(label);
    return styledTextField;
  }

  public static ResourceTextField createResourceField(Composite parent, String label, int type) {
    return createResourceField(parent, label, type, TextField.DEFAULT_LABEL_WIDTH);
  }

  public static ResourceTextField createResourceField(Composite parent, String label, int type, int labelWidth) {
    ResourceTextField field = new ResourceTextField(parent, type, labelWidth);
    field.setLabelText(label);
    return field;
  }

  public static ProposalTextField createProposalField(Composite parent, String label) {
    return createProposalField(parent, label, TextField.TYPE_LABEL);
  }

  public static ProposalTextField createProposalField(Composite parent, String label, int type) {
    return createProposalField(parent, label, type, TextField.DEFAULT_LABEL_WIDTH);
  }

  public static ProposalTextField createProposalField(Composite parent, String label, int type, int labelWidth) {
    ProposalTextField field = new ProposalTextField(parent, type, labelWidth);
    field.setLabelText(label);
    return field;
  }

  public static Group createGroupBox(Composite parent, String label) {
    Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
    group.setText(label);
    return group;
  }

  public static Button createCheckBox(Composite parent, String label, boolean checkedByDefault) {
    Button btn = new Button(parent, SWT.CHECK);
    btn.setText(label);
    btn.setSelection(checkedByDefault);
    return btn;
  }

  // Specific proposal fields

  public static ProposalTextField createPackageField(Composite parent, String label, IJavaProject project) {
    return createPackageField(parent, label, project, TextField.DEFAULT_LABEL_WIDTH);
  }

  public static ProposalTextField createPackageField(Composite parent, String label, IJavaProject project, int labelWidth) {
    return createPackageField(parent, label, project, labelWidth, TextField.TYPE_LABEL);
  }

  public static ProposalTextField createPackageField(Composite parent, String label, IJavaProject project, int labelWidth, int type) {
    ProposalTextField proposalField = createProposalField(parent, label, type, labelWidth);
    PackageContentProvider provider = new PackageContentProvider(project);
    proposalField.setContentProvider(provider);
    proposalField.setLabelProvider(provider);
    proposalField.setProposalDescriptionProvider(provider);
    return proposalField;
  }

  public static ProposalTextField createSourceFolderField(Composite parent, String label, ScoutTier tier) {
    return createSourceFolderField(parent, label, tier, TextField.DEFAULT_LABEL_WIDTH);
  }

  public static ProposalTextField createSourceFolderField(Composite parent, String label, ScoutTier tier, int labelWidth) {
    ProposalTextField proposalField = createProposalField(parent, label, TextField.TYPE_LABEL, labelWidth);
    SourceFolderContentProvider provider = new SourceFolderContentProvider(S2eScoutTier.wrap(tier));
    proposalField.setContentProvider(provider);
    proposalField.setLabelProvider(provider);
    return proposalField;
  }

  public static ProposalTextField createAbstractTypeProposalField(Composite parent, String label, IJavaProject jp, String baseClassFqn) {
    return createAbstractTypeProposalField(parent, label, jp, baseClassFqn, TextField.DEFAULT_LABEL_WIDTH);
  }

  public static ProposalTextField createAbstractTypeProposalField(Composite parent, String label, IJavaProject jp, String baseClassFqn, int labelWidth) {
    ProposalTextField proposalField = createProposalField(parent, label, TextField.TYPE_HYPERLINK, labelWidth);
    StrictHierarchyTypeContentProvider provider = new StrictHierarchyTypeContentProvider(jp, baseClassFqn);
    provider.setTypeProposalFilter(new PublicAbstractPrimaryTypeFilter());
    proposalField.setContentProvider(provider);
    proposalField.setLabelProvider(provider);
    proposalField.setProposalDescriptionProvider(provider);
    proposalField.addHyperlinkListener(new JavaElementHyperlinkListener(proposalField));
    return proposalField;
  }

  public static ProposalTextField createProjectProposalField(Composite parent, String label) {
    return createProjectProposalField(parent, label, null);
  }

  public static ProposalTextField createProjectProposalField(Composite parent, String label, Predicate<IJavaProject> filter) {
    return createProjectProposalField(parent, label, filter, TextField.DEFAULT_LABEL_WIDTH);
  }

  public static ProposalTextField createProjectProposalField(Composite parent, String label, Predicate<IJavaProject> filter, int labelWidth) {
    ProposalTextField proposalField = createProposalField(parent, label, TextField.TYPE_LABEL, labelWidth);
    JavaProjectContentProvider provider = new JavaProjectContentProvider();
    provider.setFilter(filter);
    proposalField.setContentProvider(provider);
    proposalField.setLabelProvider(provider);
    return proposalField;
  }

  public static ProposalTextField createTranslationStoreProposalField(Composite parent, String label, TranslationStoreStack stack) {
    return createTranslationStoreProposalField(parent, label, stack, TextField.DEFAULT_LABEL_WIDTH);
  }

  public static ProposalTextField createTranslationStoreProposalField(Composite parent, String label, TranslationStoreStack stack, int labelWidth) {
    ProposalTextField proposalField = createProposalField(parent, label, TextField.TYPE_LABEL, labelWidth);
    TranslationStoreContentProvider provider = new TranslationStoreContentProvider(stack);
    proposalField.setContentProvider(provider);
    proposalField.setLabelProvider(provider);
    proposalField.setProposalDescriptionProvider(provider);
    return proposalField;
  }

  public static ProposalTextField createTypeProposalField(Composite parent, String label, IJavaProject jp) {
    return createTypeProposalField(parent, label, jp, TextField.DEFAULT_LABEL_WIDTH);
  }

  public static ProposalTextField createTypeProposalField(Composite parent, String label, IJavaProject jp, int labelWidth) {
    ProposalTextField proposalField = createProposalField(parent, label, TextField.TYPE_HYPERLINK, labelWidth);
    StrictHierarchyTypeContentProvider provider = new TypeContentProvider(jp);
    provider.setTypeProposalFilter(new PublicPrimaryTypeFilter());

    proposalField.setContentProvider(provider);
    proposalField.setLabelProvider(provider);
    proposalField.setProposalDescriptionProvider(provider);

    proposalField.addHyperlinkListener(new JavaElementHyperlinkListener(proposalField));
    return proposalField;
  }

  protected static final class JavaElementHyperlinkListener extends HyperlinkAdapter {

    private final ProposalTextField m_owner;

    public JavaElementHyperlinkListener(ProposalTextField owner) {
      m_owner = owner;
    }

    @Override
    public void linkActivated(HyperlinkEvent e) {
      Object proposal = m_owner.getSelectedProposal();
      if (proposal instanceof IJavaElement) {
        S2eUiUtils.openInEditor((IJavaElement) proposal, false);
      }
    }
  }
}
