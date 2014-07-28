/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
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
import org.eclipse.scout.sdk.workspace.type.config.parser.IntegerFieldReferencePropertyParser;
import org.eclipse.scout.sdk.workspace.type.config.property.FieldProperty;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>{@link ComposerAttributeTypePresenter}</h3>
 *
 * @author Matthias Villiger
 * @since 3.10.0 02.12.2013
 */
public class ComposerAttributeTypePresenter extends AbstractProposalPresenter<FieldProperty<Integer>> {

  protected static final List<FieldProperty<Integer>> PROPOSALS;
  static {
    IType dataModelConstants = TypeUtility.getType(IRuntimeClasses.DataModelConstants);
    PROPOSALS = new ArrayList<FieldProperty<Integer>>(20);
    PROPOSALS.add(new UiFieldProperty<Integer>(dataModelConstants.getField("TYPE_INHERITED"), "Inherited"));
    PROPOSALS.add(new UiFieldProperty<Integer>(dataModelConstants.getField("TYPE_NONE"), "None"));
    PROPOSALS.add(new UiFieldProperty<Integer>(dataModelConstants.getField("TYPE_CODE_LIST"), "Code List"));
    PROPOSALS.add(new UiFieldProperty<Integer>(dataModelConstants.getField("TYPE_CODE_TREE"), "Code Tree"));
    PROPOSALS.add(new UiFieldProperty<Integer>(dataModelConstants.getField("TYPE_NUMBER_LIST"), "Number List"));
    PROPOSALS.add(new UiFieldProperty<Integer>(dataModelConstants.getField("TYPE_NUMBER_TREE"), "Number Tree"));
    PROPOSALS.add(new UiFieldProperty<Integer>(dataModelConstants.getField("TYPE_DATE"), "Date"));
    PROPOSALS.add(new UiFieldProperty<Integer>(dataModelConstants.getField("TYPE_TIME"), "Time"));
    PROPOSALS.add(new UiFieldProperty<Integer>(dataModelConstants.getField("TYPE_DATE_TIME"), "Date-Time"));
    PROPOSALS.add(new UiFieldProperty<Integer>(dataModelConstants.getField("TYPE_INTEGER"), "Integer"));
    PROPOSALS.add(new UiFieldProperty<Integer>(dataModelConstants.getField("TYPE_LONG"), "Long"));
    PROPOSALS.add(new UiFieldProperty<Integer>(dataModelConstants.getField("TYPE_DOUBLE"), "Double"));
    PROPOSALS.add(new UiFieldProperty<Integer>(dataModelConstants.getField("TYPE_PLAIN_INTEGER"), "Plain Integer"));
    PROPOSALS.add(new UiFieldProperty<Integer>(dataModelConstants.getField("TYPE_PLAIN_LONG"), "Plain Long"));
    PROPOSALS.add(new UiFieldProperty<Integer>(dataModelConstants.getField("TYPE_PLAIN_DOUBLE"), "Plain Double"));
    PROPOSALS.add(new UiFieldProperty<Integer>(dataModelConstants.getField("TYPE_PERCENT"), "Percent"));
    PROPOSALS.add(new UiFieldProperty<Integer>(dataModelConstants.getField("TYPE_STRING"), "String"));
    PROPOSALS.add(new UiFieldProperty<Integer>(dataModelConstants.getField("TYPE_SMART"), "Smart"));
    PROPOSALS.add(new UiFieldProperty<Integer>(dataModelConstants.getField("TYPE_AGGREGATE_COUNT"), "Aggregate Count"));
    PROPOSALS.add(new UiFieldProperty<Integer>(dataModelConstants.getField("TYPE_FULL_TEXT"), "Full Text"));
  }

  private final AbstractFieldReferencePropertyParser<Integer> m_parser;

  public ComposerAttributeTypePresenter(PropertyViewFormToolkit toolkit, Composite parent) {
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

  public AbstractFieldReferencePropertyParser<Integer> getParser() {
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
