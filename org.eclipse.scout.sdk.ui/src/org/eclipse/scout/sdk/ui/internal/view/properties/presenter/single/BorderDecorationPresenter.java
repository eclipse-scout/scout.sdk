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
import org.eclipse.scout.sdk.workspace.type.config.parser.AbstractFieldReferencePropertyParser;
import org.eclipse.scout.sdk.workspace.type.config.parser.StringFieldReferencePropertyParser;
import org.eclipse.scout.sdk.workspace.type.config.property.FieldProperty;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>{@link BorderDecorationPresenter}</h3>
 *
 * @author Matthias Villiger
 * @since 3.8.0 04.12.2012
 */
public class BorderDecorationPresenter extends AbstractProposalPresenter<FieldProperty<String>> {

  protected static final UiFieldProperty<String> BORDER_DECORATION_EMPTY;
  protected static final UiFieldProperty<String> BORDER_DECORATION_LINE;
  protected static final UiFieldProperty<String> BORDER_DECORATION_SECTION;
  protected static final UiFieldProperty<String> BORDER_DECORATION_AUTO;

  protected static final List<FieldProperty<String>> PROPOSALS;
  static {
    IType iGroupBox = TypeUtility.getType(IRuntimeClasses.IGroupBox);
    BORDER_DECORATION_EMPTY = new UiFieldProperty<>(iGroupBox.getField("BORDER_DECORATION_EMPTY"), "empty");
    BORDER_DECORATION_LINE = new UiFieldProperty<>(iGroupBox.getField("BORDER_DECORATION_LINE"), "line");
    BORDER_DECORATION_SECTION = new UiFieldProperty<>(iGroupBox.getField("BORDER_DECORATION_SECTION"), "section");
    BORDER_DECORATION_AUTO = new UiFieldProperty<>(iGroupBox.getField("BORDER_DECORATION_AUTO"), "auto");
    PROPOSALS = new ArrayList<>(4);
    PROPOSALS.add(BORDER_DECORATION_EMPTY);
    PROPOSALS.add(BORDER_DECORATION_LINE);
    PROPOSALS.add(BORDER_DECORATION_SECTION);
    PROPOSALS.add(BORDER_DECORATION_AUTO);
  }

  private final AbstractFieldReferencePropertyParser<String> m_parser;

  public BorderDecorationPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
    m_parser = new StringFieldReferencePropertyParser(PROPOSALS);
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

  public AbstractFieldReferencePropertyParser<String> getParser() {
    return m_parser;
  }

  @Override
  protected FieldProperty<String> parseInput(String input) throws CoreException {
    return getParser().parseSourceValue(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
  }

  @Override
  protected synchronized void storeValue(FieldProperty<String> value) throws CoreException {
    if (value == null) {
      getProposalField().acceptProposal(getDefaultValue());
      value = getDefaultValue();
    }

    try {
      ConfigPropertyUpdateOperation<FieldProperty<String>> updateOp = new ConfigPropertyUpdateOperation<>(getMethod(), getParser());
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
