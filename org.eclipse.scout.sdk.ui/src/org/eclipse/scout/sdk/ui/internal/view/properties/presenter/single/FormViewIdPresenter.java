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
import org.eclipse.scout.sdk.workspace.type.config.parser.StringFieldReferencePropertyParser;
import org.eclipse.scout.sdk.workspace.type.config.property.FieldProperty;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>FormViewIdPresenter</h3> ...
 */
public class FormViewIdPresenter extends AbstractProposalPresenter<FieldProperty<String>> {
  protected static final UiFieldProperty<String> VIEW_ID_N;
  protected static final UiFieldProperty<String> VIEW_ID_NE;
  protected static final UiFieldProperty<String> VIEW_ID_E;
  protected static final UiFieldProperty<String> VIEW_ID_SE;
  protected static final UiFieldProperty<String> VIEW_ID_S;
  protected static final UiFieldProperty<String> VIEW_ID_SW;
  protected static final UiFieldProperty<String> VIEW_ID_W;
  protected static final UiFieldProperty<String> VIEW_ID_NW;
  protected static final UiFieldProperty<String> VIEW_ID_CENTER;
  protected static final UiFieldProperty<String> VIEW_ID_OUTLINE;
  protected static final UiFieldProperty<String> VIEW_ID_OUTLINE_SELECTOR;
  protected static final UiFieldProperty<String> VIEW_ID_PAGE_DETAIL;
  protected static final UiFieldProperty<String> VIEW_ID_PAGE_SEARCH;
  protected static final UiFieldProperty<String> VIEW_ID_PAGE_TABLE;

  protected static final List<FieldProperty<String>> PROPOSALS;
  static {
    IType iForm = TypeUtility.getType(IRuntimeClasses.IForm);
    VIEW_ID_N = new UiFieldProperty<String>(iForm.getField("VIEW_ID_N"), "north");
    VIEW_ID_NE = new UiFieldProperty<String>(iForm.getField("VIEW_ID_NE"), "north-east");
    VIEW_ID_E = new UiFieldProperty<String>(iForm.getField("VIEW_ID_E"), "east");
    VIEW_ID_SE = new UiFieldProperty<String>(iForm.getField("VIEW_ID_SE"), "south-east");
    VIEW_ID_S = new UiFieldProperty<String>(iForm.getField("VIEW_ID_S"), "south");
    VIEW_ID_SW = new UiFieldProperty<String>(iForm.getField("VIEW_ID_SW"), "south-west");
    VIEW_ID_W = new UiFieldProperty<String>(iForm.getField("VIEW_ID_W"), "west");
    VIEW_ID_NW = new UiFieldProperty<String>(iForm.getField("VIEW_ID_NW"), "north-west");
    VIEW_ID_CENTER = new UiFieldProperty<String>(iForm.getField("VIEW_ID_CENTER"), "center");
    VIEW_ID_OUTLINE = new UiFieldProperty<String>(iForm.getField("VIEW_ID_OUTLINE"), "outline");
    VIEW_ID_OUTLINE_SELECTOR = new UiFieldProperty<String>(iForm.getField("VIEW_ID_OUTLINE_SELECTOR"), "outline-selector");
    VIEW_ID_PAGE_DETAIL = new UiFieldProperty<String>(iForm.getField("VIEW_ID_PAGE_DETAIL"), "page-detail");
    VIEW_ID_PAGE_SEARCH = new UiFieldProperty<String>(iForm.getField("VIEW_ID_PAGE_SEARCH"), "page-search");
    VIEW_ID_PAGE_TABLE = new UiFieldProperty<String>(iForm.getField("VIEW_ID_PAGE_TABLE"), "page-table");

    PROPOSALS = new ArrayList<FieldProperty<String>>(14);
    PROPOSALS.add(VIEW_ID_N);
    PROPOSALS.add(VIEW_ID_NE);
    PROPOSALS.add(VIEW_ID_E);
    PROPOSALS.add(VIEW_ID_SE);
    PROPOSALS.add(VIEW_ID_S);
    PROPOSALS.add(VIEW_ID_SW);
    PROPOSALS.add(VIEW_ID_W);
    PROPOSALS.add(VIEW_ID_NW);
    PROPOSALS.add(VIEW_ID_CENTER);
    PROPOSALS.add(VIEW_ID_OUTLINE);
    PROPOSALS.add(VIEW_ID_OUTLINE_SELECTOR);
    PROPOSALS.add(VIEW_ID_PAGE_DETAIL);
    PROPOSALS.add(VIEW_ID_PAGE_SEARCH);
    PROPOSALS.add(VIEW_ID_PAGE_TABLE);
  }

  private final FieldReferencePropertyParser<String> m_parser;

  public FormViewIdPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
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

  public FieldReferencePropertyParser<String> getParser() {
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
      ConfigPropertyUpdateOperation<FieldProperty<String>> updateOp = new ConfigPropertyUpdateOperation<FieldProperty<String>>(getMethod(), getParser());
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
