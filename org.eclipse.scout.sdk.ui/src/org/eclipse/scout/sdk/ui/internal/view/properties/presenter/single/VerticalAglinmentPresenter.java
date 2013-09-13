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
import org.eclipse.scout.sdk.workspace.type.config.parser.IntegerSourcePropertyParser;
import org.eclipse.scout.sdk.workspace.type.config.parser.SourcePropertyParser;
import org.eclipse.scout.sdk.workspace.type.config.property.SourceProperty;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>VerticalAglinmentPresenter</h3> ...
 */
public class VerticalAglinmentPresenter extends AbstractProposalPresenter<SourceProperty<Integer>> {
  protected static final SourceProperty<Integer> TOP;
  protected static final SourceProperty<Integer> CENTER;
  protected static final SourceProperty<Integer> BOTTOM;

  protected static final List<SourceProperty<Integer>> PROPOSALS;
  static {
    TOP = new UiSourceProperty<Integer>(Integer.valueOf(-1), "top");
    CENTER = new UiSourceProperty<Integer>(Integer.valueOf(0), "center");
    BOTTOM = new UiSourceProperty<Integer>(Integer.valueOf(1), "bottom");
    PROPOSALS = new ArrayList<SourceProperty<Integer>>();
    PROPOSALS.add(TOP);
    PROPOSALS.add(CENTER);
    PROPOSALS.add(BOTTOM);
  }

  private final SourcePropertyParser<Integer> m_parser;

  public VerticalAglinmentPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
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
        if (CompareUtility.equals(TOP, element)) {
          return ScoutSdkUi.getImage(ScoutSdkUi.VerticalTop);
        }
        else if (CompareUtility.equals(CENTER, element)) {
          return ScoutSdkUi.getImage(ScoutSdkUi.VerticalCenter);
        }
        else if (CompareUtility.equals(BOTTOM, element)) {
          return ScoutSdkUi.getImage(ScoutSdkUi.VerticalBottom);
        }
        else {
          return ScoutSdkUi.getImage(ScoutSdkUi.Default);
        }
      }
    };
    getProposalField().setLabelProvider(labelProvider);
    StaticContentProvider provider = new StaticContentProvider(PROPOSALS.toArray(new UiSourceProperty[PROPOSALS.size()]), labelProvider);
    getProposalField().setContentProvider(provider);
  }

  public SourcePropertyParser<Integer> getParser() {
    return m_parser;
  }

  @Override
  protected SourceProperty<Integer> parseInput(String input) throws CoreException {
    return getParser().parseSourceValue(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
  }

  @Override
  protected synchronized void storeValue(SourceProperty<Integer> value) throws CoreException {
    if (value == null) {
      getProposalField().acceptProposal(getDefaultValue());
      value = getDefaultValue();
    }
    try {
      ConfigPropertyUpdateOperation<SourceProperty<Integer>> updateOp = new ConfigPropertyUpdateOperation<SourceProperty<Integer>>(getMethod(), getParser());
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
