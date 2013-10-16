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
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.scout.sdk.extensions.runtime.classes.IRuntimeClasses;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.StaticContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.styled.SearchRangeStyledLabelProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractProposalPresenter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.config.parser.FieldReferencePropertyParser;
import org.eclipse.scout.sdk.workspace.type.config.parser.IntegerFieldReferencePropertyParser;
import org.eclipse.scout.sdk.workspace.type.config.property.FieldProperty;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>FormDisplayHintPresenter</h3> ...
 */
public class FormDisplayHintPresenter extends AbstractProposalPresenter<FieldProperty<Integer>> {
  protected static final UiFieldProperty<Integer> DISPLAY_HINT_DIALOG;
  protected static final UiFieldProperty<Integer> DISPLAY_HINT_POPUP_WINDOW;
  protected static final UiFieldProperty<Integer> DISPLAY_HINT_POPUP_DIALOG;
  protected static final UiFieldProperty<Integer> DISPLAY_HINT_VIEW;

  protected static final List<FieldProperty<Integer>> PROPOSALS;
  static {
    IType iForm = TypeUtility.getType(IRuntimeClasses.IForm);
    DISPLAY_HINT_DIALOG = new UiFieldProperty<Integer>(iForm.getField("DISPLAY_HINT_DIALOG"), "dialog");
    DISPLAY_HINT_POPUP_WINDOW = new UiFieldProperty<Integer>(iForm.getField("DISPLAY_HINT_POPUP_WINDOW"), "popup window");
    DISPLAY_HINT_POPUP_DIALOG = new UiFieldProperty<Integer>(iForm.getField("DISPLAY_HINT_POPUP_DIALOG"), "popup dialog");
    DISPLAY_HINT_VIEW = new UiFieldProperty<Integer>(iForm.getField("DISPLAY_HINT_VIEW"), "view");
    PROPOSALS = new ArrayList<FieldProperty<Integer>>(4);
    PROPOSALS.add(DISPLAY_HINT_DIALOG);
    PROPOSALS.add(DISPLAY_HINT_POPUP_WINDOW);
    PROPOSALS.add(DISPLAY_HINT_POPUP_DIALOG);
    PROPOSALS.add(DISPLAY_HINT_VIEW);
  }

  private final FieldReferencePropertyParser<Integer> m_parser;

  public FormDisplayHintPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
    m_parser = new IntegerFieldReferencePropertyParser(PROPOSALS);
  }

  @Override
  protected void createProposalFieldProviders(ProposalTextField proposalField) {
    ILabelProvider labelProvider = new SearchRangeStyledLabelProvider() {
      @Override
      public String getText(Object element) {
        return element.toString();
      }

      @Override
      public Image getImage(Object element) {
        return ScoutSdkUi.getImage(ScoutSdkUi.Default);
      }

    };
    getProposalField().setLabelProvider(labelProvider);
    StaticContentProvider provider = new StaticContentProvider(PROPOSALS.toArray(new FieldProperty[PROPOSALS.size()]), labelProvider);
    getProposalField().setContentProvider(provider);
  }

  public FieldReferencePropertyParser<Integer> getParser() {
    return m_parser;
  }

  @Override
  protected FieldProperty<Integer> parseInput(String input) throws CoreException {
    return getParser().parseSourceValue(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
  }

  @Override
  protected synchronized void storeValue(FieldProperty<Integer> value) throws CoreException {
    if (value == null) {
      getProposalField().acceptProposal(getDefaultValue());
      value = getDefaultValue();
    }

    try {
      ConfigPropertyUpdateOperation<FieldProperty<Integer>> updateOp = new ConfigPropertyUpdateOperation<FieldProperty<Integer>>(getMethod(), getParser());
      updateOp.setValue(value);
      OperationJob job = new OperationJob(updateOp);
      job.setDebug(true);
      job.schedule();
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could not parse default value of method '" + getMethod().getMethodName() + "' in type '" + getMethod().getType().getFullyQualifiedName() + "'.", e);
    }

  }

}
