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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.StaticContentProvider;
import org.eclipse.scout.sdk.ui.fields.proposal.styled.SearchRangeStyledLabelProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractProposalPresenter;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>HorizontalAglinmentPresenter</h3> ...
 */
public class HorizontalAlignmentPresenter extends AbstractProposalPresenter<HorizontalAlignmentPresenter.HorizontalAlignment> {

  protected static enum HorizontalAlignment {
    Left,
    Center,
    Right
  }

  public HorizontalAlignmentPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);

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
        HorizontalAlignment value = (HorizontalAlignment) element;
        switch (value) {
          case Left:
            return ScoutSdkUi.getImage(ScoutSdkUi.HorizontalLeft);
          case Center:
            return ScoutSdkUi.getImage(ScoutSdkUi.HorizontalCenter);
          case Right:
            return ScoutSdkUi.getImage(ScoutSdkUi.HorizontalRight);
        }
        return null;
      }

    };
    getProposalField().setLabelProvider(labelProvider);
    StaticContentProvider provider = new StaticContentProvider(HorizontalAlignment.values(), labelProvider);
    getProposalField().setContentProvider(provider);
  }

  @Override
  protected HorizontalAlignment parseInput(String input) throws CoreException {
    int parsedInt = PropertyMethodSourceUtility.parseReturnParameterInteger(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    if (parsedInt < 0) {
      return HorizontalAlignment.Left;
    }
    else if (parsedInt == 0) {
      return HorizontalAlignment.Center;
    }
    else {
      return HorizontalAlignment.Right;
    }
  }

  @Override
  protected synchronized void storeValue(HorizontalAlignment value) throws CoreException {
    if (value == null) {
      // set to default
      getProposalField().acceptProposal(getDefaultValue());
      value = getDefaultValue();
    }
    //IField field = value.getField();
    IOperation op = null;
    if (UiUtility.equals(getDefaultValue(), value)) {
      if (getMethod().isImplemented()) {
        op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
      }
    }
    else {
      StringBuilder source = new StringBuilder("return ");
      switch (value) {
        case Left:
          source.append("-1");
          break;
        case Center:
          source.append("0");
          break;
        case Right:
          source.append("1");
          break;
      }
      source.append(";");
      op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName(), source.toString(), true);
    }
    if (op != null) {
      OperationJob job = new OperationJob(op);
      job.schedule();
    }
  }

}
