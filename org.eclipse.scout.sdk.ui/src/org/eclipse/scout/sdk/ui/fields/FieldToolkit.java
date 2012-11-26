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
package org.eclipse.scout.sdk.ui.fields;

import java.util.ArrayList;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.SiblingProposal;
import org.eclipse.scout.sdk.ui.fields.proposal.SimpleLabelProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.SimpleProposalProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.javaelement.AbstractJavaElementContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.nls.NlsProposalDescriptionProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.nls.NlsTextContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.nls.NlsTextLabelProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.nls.NlsTextSelectionHandler;
import org.eclipse.scout.sdk.ui.fields.proposal.signature.SignatureLabelProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.signature.SignatureProposalProvider;
import org.eclipse.scout.sdk.util.type.ITypeFilter;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.widgets.Composite;

public class FieldToolkit {

  public StyledTextField createStyledTextField(Composite parent, String label) {
    return new StyledTextField(parent, label);
  }

  public StyledTextField createStyledTextField(Composite parent, String label, int labelPercentage) {
    return new StyledTextField(parent, label, labelPercentage);
  }

  public ProposalTextField createProposalField(Composite parent, String label) {
    return createProposalField(parent, label, ProposalTextField.STYLE_DEFAULT);
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

  public ProposalTextField createNlsProposalTextField(Composite parent, INlsProject nlsProject, String label) {
    return createNlsProposalTextField(parent, nlsProject, label, TextField.DEFAULT_LABEL_PERCENTAGE);
  }

  public ProposalTextField createNlsProposalTextField(Composite parent, INlsProject nlsProject, String label, int labelPercentage) {
    if (nlsProject == null) {
      throw new IllegalArgumentException("nlsProject can not be null!");
    }
    ProposalTextField field = createProposalField(parent, label, ProposalTextField.STYLE_DEFAULT, labelPercentage);
    NlsTextLabelProvider labelProvider = new NlsTextLabelProvider(nlsProject);
    field.setLabelProvider(labelProvider);
    field.setContentProvider(new NlsTextContentProvider(labelProvider));
    field.setSelectionHandler(new NlsTextSelectionHandler(nlsProject));
    field.setProposalDescriptionProvider(new NlsProposalDescriptionProvider());
    return field;
  }

  public ProposalTextField createSignatureProposalField(Composite parent, String label, IScoutBundle bundle) {
    return createSignatureProposalField(parent, label, bundle, null);
  }

  public ProposalTextField createSignatureProposalField(Composite parent, String label, IScoutBundle bundle, String[] mostlyUsed) {
    return createSignatureProposalField(parent, label, bundle, mostlyUsed, TextField.DEFAULT_LABEL_PERCENTAGE);
  }

  public ProposalTextField createSignatureProposalField(Composite parent, String label, IScoutBundle bundle, String[] mostlyUsed, int labelPercentage) {
    ProposalTextField field = createProposalField(parent, label, ProposalTextField.STYLE_DEFAULT, labelPercentage);
    SignatureLabelProvider labelProvider = new SignatureLabelProvider();
    field.setLabelProvider(labelProvider);
    SignatureProposalProvider proposalProvider = new SignatureProposalProvider(bundle.getSearchScope(), labelProvider, mostlyUsed, false);
    field.setContentProvider(proposalProvider);
    return field;
  }

  public ProposalTextField createSiblingProposalField(Composite parent, IType declaringType, IType siblingDeclaringType) {
    ITypeHierarchy localHierarchy = TypeUtility.getLocalTypeHierarchy(declaringType);
    return createSiblingProposalField(parent, declaringType, siblingDeclaringType, localHierarchy);
  }

  public ProposalTextField createSiblingProposalField(Composite parent, IType declaringType, IType siblingDeclaringType, ITypeHierarchy hierarchy) {
    return createSiblingProposalField(parent, declaringType, siblingDeclaringType, hierarchy, TextField.DEFAULT_LABEL_PERCENTAGE);
  }

  public ProposalTextField createSiblingProposalField(Composite parent, IType declaringType, IType siblingDeclaringType, ITypeHierarchy hierarchy, int labelPercentage) {
    ITypeFilter filter = TypeFilters.getMultiTypeFilter(TypeFilters.getClassFilter(), TypeFilters.getSubtypeFilter(siblingDeclaringType, hierarchy));
    IType[] innerTypes = TypeUtility.getInnerTypes(declaringType, filter, ScoutTypeComparators.getOrderAnnotationComparator());
    ArrayList<SiblingProposal> siblingList = new ArrayList<SiblingProposal>();
    for (IJavaElement t : innerTypes) {
      siblingList.add(new SiblingProposal(t));
    }
    siblingList.add(SiblingProposal.SIBLING_END);
    ProposalTextField field = createProposalField(parent, Texts.get("Sibling"), ProposalTextField.STYLE_DEFAULT, labelPercentage);
    field.setEnabled(siblingList.size() > 1);
    field.setLabelProvider(new SimpleLabelProvider());
    field.setContentProvider(new SimpleProposalProvider(siblingList.toArray(new SiblingProposal[siblingList.size()])));
    return field;
  }

  /**
   * @param parent
   * @param declaringType
   * @return
   */
  public ProposalTextField createFormFieldSiblingProposalField(Composite parent, IType declaringType) {
    return createFormFieldSiblingProposalField(parent, declaringType, TextField.DEFAULT_LABEL_PERCENTAGE);
  }

  public ProposalTextField createFormFieldSiblingProposalField(Composite parent, IType declaringType, int labelPercentage) {
    ITypeHierarchy localHierarchy = TypeUtility.getLocalTypeHierarchy(declaringType);
    ProposalTextField field = createSiblingProposalField(parent, declaringType, TypeUtility.getType(RuntimeClasses.IFormField), localHierarchy, labelPercentage);
    SiblingProposal selectedProposal = SiblingProposal.SIBLING_END;
    IType firstButton = ScoutTypeUtility.getFistProcessButton(declaringType, localHierarchy);
    if (firstButton != null) {
      selectedProposal = new SiblingProposal(firstButton);
    }
    field.acceptProposal(selectedProposal);
    return field;
  }

  public ProposalTextField createJavaElementProposalField(Composite parent, String label, AbstractJavaElementContentProvider contentProvider) {
    return createJavaElementProposalField(parent, label, contentProvider, TextField.DEFAULT_LABEL_PERCENTAGE);
  }

  public ProposalTextField createJavaElementProposalField(Composite parent, String label, AbstractJavaElementContentProvider contentProvider, int labelPercentage) {
    ProposalTextField field = createProposalField(parent, label, ProposalTextField.STYLE_DEFAULT, labelPercentage);
    field.setLabelProvider(contentProvider.getLabelProvider());
    field.setContentProvider(contentProvider);
    return field;
  }
}
