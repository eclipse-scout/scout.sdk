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
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.ButtonDisplayStylePresenter.ButtonStyle;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractProposalPresenter;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>ButtonDisplayTypePresenter</h3> ...
 */
public class ButtonDisplayStylePresenter extends AbstractProposalPresenter<ButtonStyle> {

  protected static enum ButtonStyle {
    Default,
    Toggle,
    Radio,
    Link
  }

  public ButtonDisplayStylePresenter(PropertyViewFormToolkit toolkit, Composite parent) {
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
        return ScoutSdkUi.getImage(ScoutSdkUi.ButtonStyle);
      }

    };
    getProposalField().setLabelProvider(labelProvider);
    StaticContentProvider provider = new StaticContentProvider(ButtonStyle.values(), labelProvider);
    getProposalField().setContentProvider(provider);
  }

  @Override
  protected ButtonStyle parseInput(String input) throws CoreException {
    int parsedInt = PropertyMethodSourceUtility.parseReturnParameterInteger(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    switch (parsedInt) {
      case 0:
        return ButtonStyle.Default;
      case 1:
        return ButtonStyle.Toggle;
      case 2:
        return ButtonStyle.Radio;
      case 3:
        return ButtonStyle.Link;
    }
    return null;
  }

  @Override
  protected synchronized void storeValue(ButtonStyle value) {
    IOperation op = null;
    if (UiUtility.equals(getDefaultValue(), value)) {
      if (getMethod().isImplemented()) {
        op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
      }
    }
    else {
      String sourceValue = null;
      switch (value) {
        case Default:
          sourceValue = "DISPLAY_STYLE_DEFAULT";
          break;
        case Toggle:
          sourceValue = "DISPLAY_STYLE_RADIO";
          break;
        case Radio:
          sourceValue = "DISPLAY_STYLE_TOGGLE";
          break;
        case Link:
          sourceValue = "DISPLAY_STYLE_LINK";
          break;
      }
      op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName(), "  return " + sourceValue + ";", false);
    }
    if (op != null) {
      new OperationJob(op).schedule();
    }
  }

}
