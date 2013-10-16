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
 * <h3>ButtonSystemTypePresenter</h3> ...
 */
public class ButtonSystemTypePresenter extends AbstractProposalPresenter<FieldProperty<Integer>> {

  protected static final UiFieldProperty<Integer> SYSTEM_TYPE_NONE;
  protected static final UiFieldProperty<Integer> SYSTEM_TYPE_CANCEL;
  protected static final UiFieldProperty<Integer> SYSTEM_TYPE_CLOSE;
  protected static final UiFieldProperty<Integer> SYSTEM_TYPE_OK;
  protected static final UiFieldProperty<Integer> SYSTEM_TYPE_RESET;
  protected static final UiFieldProperty<Integer> SYSTEM_TYPE_SAVE;
  protected static final UiFieldProperty<Integer> SYSTEM_TYPE_SAVE_WITHOUT_MARKER_CHANGE;

  protected static final List<FieldProperty<Integer>> PROPOSALS;
  static {
    IType iButton = TypeUtility.getType(IRuntimeClasses.IButton);
    SYSTEM_TYPE_NONE = new UiFieldProperty<Integer>(iButton.getField("SYSTEM_TYPE_NONE"), "none");
    SYSTEM_TYPE_CANCEL = new UiFieldProperty<Integer>(iButton.getField("SYSTEM_TYPE_CANCEL"), "cancel");
    SYSTEM_TYPE_CLOSE = new UiFieldProperty<Integer>(iButton.getField("SYSTEM_TYPE_CLOSE"), "close");
    SYSTEM_TYPE_OK = new UiFieldProperty<Integer>(iButton.getField("SYSTEM_TYPE_OK"), "ok");
    SYSTEM_TYPE_RESET = new UiFieldProperty<Integer>(iButton.getField("SYSTEM_TYPE_RESET"), "ok");
    SYSTEM_TYPE_SAVE = new UiFieldProperty<Integer>(iButton.getField("SYSTEM_TYPE_SAVE"), "ok");
    SYSTEM_TYPE_SAVE_WITHOUT_MARKER_CHANGE = new UiFieldProperty<Integer>(iButton.getField("SYSTEM_TYPE_SAVE_WITHOUT_MARKER_CHANGE"), "save without change marker");

    PROPOSALS = new ArrayList<FieldProperty<Integer>>(7);
    PROPOSALS.add(SYSTEM_TYPE_NONE);
    PROPOSALS.add(SYSTEM_TYPE_CANCEL);
    PROPOSALS.add(SYSTEM_TYPE_CLOSE);
    PROPOSALS.add(SYSTEM_TYPE_OK);
    PROPOSALS.add(SYSTEM_TYPE_RESET);
    PROPOSALS.add(SYSTEM_TYPE_SAVE);
    PROPOSALS.add(SYSTEM_TYPE_SAVE_WITHOUT_MARKER_CHANGE);

  }

  private final FieldReferencePropertyParser<Integer> m_parser;

  public ButtonSystemTypePresenter(PropertyViewFormToolkit toolkit, Composite parent) {
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
