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
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.fields.proposal.ProposalTextField;
import org.eclipse.scout.sdk.ui.fields.proposal.StaticContentProvider;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.VerticalAglinmentPresenter.VerticalAlignment;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractProposalPresenter;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>VerticalAglinmentPresenter</h3> ...
 */
public class VerticalAglinmentPresenter extends AbstractProposalPresenter<VerticalAlignment> {

  protected static enum VerticalAlignment {
    Top,
    Center,
    Bottom
  }

  public VerticalAglinmentPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
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
        VerticalAlignment value = (VerticalAlignment) element;
        switch (value) {
          case Top:
            return ScoutSdkUi.getImage(ScoutSdkUi.VerticalTop);
          case Center:
            return ScoutSdkUi.getImage(ScoutSdkUi.VerticalCenter);
          case Bottom:
            return ScoutSdkUi.getImage(ScoutSdkUi.VerticalBottom);
        }
        return null;
      }

    };
    getProposalField().setLabelProvider(labelProvider);
    StaticContentProvider provider = new StaticContentProvider(VerticalAlignment.values(), labelProvider);
    getProposalField().setContentProvider(provider);
  }

  @Override
  protected VerticalAlignment parseInput(String input) throws CoreException {
    int parsedInt = PropertyMethodSourceUtility.parseReturnParameterInteger(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    if (parsedInt < 0) {
      return VerticalAlignment.Top;
    }
    else if (parsedInt == 0) {
      return VerticalAlignment.Center;
    }
    else {
      return VerticalAlignment.Bottom;
    }
  }

  @Override
  protected synchronized void storeValue(VerticalAlignment value) throws CoreException {
    if (value == null) {
      // set to default
      getProposalField().acceptProposal(getDefaultValue());
      value = getDefaultValue();
    }
    IOperation op = null;
    if (UiUtility.equals(getDefaultValue(), value)) {
      if (getMethod().isImplemented()) {
        op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
      }
    }
    else {
      StringBuilder source = new StringBuilder("return ");
      switch (value) {
        case Top:
          source.append("-1");
          break;
        case Center:
          source.append("0");
          break;
        case Bottom:
          source.append("1");
          break;
      }
      source.append(";");
      op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName(), source.toString(), true);
    }
    if (op != null) {
      new OperationJob(op).schedule();
    }
  }

}
