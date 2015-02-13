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
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.StaticContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractProposalPresenter;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.config.parser.AbstractSourcePropertyParser;
import org.eclipse.scout.sdk.workspace.type.config.parser.IntegerSourcePropertyParser;
import org.eclipse.scout.sdk.workspace.type.config.property.AbstractSourceProperty;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class LabelHorizontalAlignmentPresenter extends AbstractProposalPresenter<AbstractSourceProperty<Integer>> {

  protected static final AbstractSourceProperty<Integer> LEFT;
  protected static final AbstractSourceProperty<Integer> CENTER;
  protected static final AbstractSourceProperty<Integer> RIGHT;
  protected static final AbstractSourceProperty<Integer> DEFAULT;

  protected static final List<AbstractSourceProperty<Integer>> PROPOSALS;
  static {
    LEFT = new UiSourceProperty<>(Integer.valueOf(-1), "left");
    CENTER = new UiSourceProperty<>(Integer.valueOf(0), "center");
    RIGHT = new UiSourceProperty<>(Integer.valueOf(1), "right");
    DEFAULT = new UiSourceProperty<>(Integer.valueOf(1000), "default");
    PROPOSALS = new ArrayList<>(4);
    PROPOSALS.add(LEFT);
    PROPOSALS.add(CENTER);
    PROPOSALS.add(RIGHT);
    PROPOSALS.add(DEFAULT);
  }

  private final AbstractSourcePropertyParser<Integer> m_parser;

  public LabelHorizontalAlignmentPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
    m_parser = new IntegerSourcePropertyParser(PROPOSALS);
  }

  @Override
  protected void createProposalFieldProviders(ProposalTextField proposalField) {
    ILabelProvider labelProvider = new LabelProvider() {
      @Override
      public String getText(Object element) {
        return element.toString();
      }

      @Override
      public Image getImage(Object element) {
        if (CompareUtility.equals(LEFT, element)) {
          return ScoutSdkUi.getImage(ScoutSdkUi.HorizontalLeft);
        }
        else if (CompareUtility.equals(CENTER, element)) {
          return ScoutSdkUi.getImage(ScoutSdkUi.HorizontalCenter);
        }
        else if (CompareUtility.equals(RIGHT, element)) {
          return ScoutSdkUi.getImage(ScoutSdkUi.HorizontalRight);
        }
        return ScoutSdkUi.getImage(ScoutSdkUi.Default);
      }

    };
    getProposalField().setLabelProvider(labelProvider);
    StaticContentProvider provider = new StaticContentProvider(PROPOSALS.toArray(new UiSourceProperty[PROPOSALS.size()]), labelProvider);
    getProposalField().setContentProvider(provider);
  }

  public AbstractSourcePropertyParser<Integer> getParser() {
    return m_parser;
  }

  @Override
  protected AbstractSourceProperty<Integer> parseInput(String input) throws CoreException {
    return getParser().parseSourceValue(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
  }

  @Override
  protected synchronized void storeValue(AbstractSourceProperty<Integer> value) throws CoreException {
    if (value == null) {
      getProposalField().acceptProposal(getDefaultValue());
      value = getDefaultValue();
    }
    try {
      ConfigPropertyUpdateOperation<AbstractSourceProperty<Integer>> updateOp = new ConfigPropertyUpdateOperation<>(getMethod(), getParser());
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
