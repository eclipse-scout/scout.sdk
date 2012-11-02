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
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.ButtonSystemTypePresenter.SystemType;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractProposalPresenter;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>ButtonSystemTypePresenter</h3> ...
 */
public class ButtonSystemTypePresenter extends AbstractProposalPresenter<SystemType> {

  protected static enum SystemType {
    Cancel,
    Close,
    None,
    Ok,
    Reset,
    Save,
    SaveWithoutMarkerChange
  }

  public ButtonSystemTypePresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected void createProposalFieldProviders(ProposalTextField proposalField) {
    ILabelProvider labelProvider = new SearchRangeStyledLabelProvider() {
      @Override
      public String getText(Object element) {
        SystemType value = (SystemType) element;
        switch (value) {
          case SaveWithoutMarkerChange:
            return "Save without marker change";
          default:
            return value.toString();
        }
      }

      @Override
      public Image getImage(Object element) {
        return ScoutSdkUi.getImage(ScoutSdkUi.Default);
      }

    };
    getProposalField().setLabelProvider(labelProvider);
    StaticContentProvider provider = new StaticContentProvider(SystemType.values(), labelProvider);
    getProposalField().setContentProvider(provider);
  }

  @Override
  protected SystemType parseInput(String input) throws CoreException {
    int parsedInt = PropertyMethodSourceUtility.parseReturnParameterInteger(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    switch (parsedInt) {
      case 0:
        return SystemType.None;
      case 1:
        return SystemType.Cancel;
      case 2:
        return SystemType.Close;
      case 3:
        return SystemType.Ok;
      case 4:
        return SystemType.Reset;
      case 5:
        return SystemType.Save;
      case 6:
        return SystemType.SaveWithoutMarkerChange;
    }
    return null;
  }

  @Override
  protected synchronized void storeValue(SystemType value) {
    IOperation op = null;
    if (UiUtility.equals(getDefaultValue(), value)) {
      if (getMethod().isImplemented()) {
        op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
      }
    }
    else {
      String sourceValue = null;
      switch (value) {
        case Cancel:
          sourceValue = "SYSTEM_TYPE_CANCEL";
          break;
        case Close:
          sourceValue = "SYSTEM_TYPE_CLOSE";
          break;
        case None:
          sourceValue = "SYSTEM_TYPE_NONE";
          break;
        case Ok:
          sourceValue = "SYSTEM_TYPE_OK";
          break;
        case Reset:
          sourceValue = "SYSTEM_TYPE_RESET";
          break;
        case Save:
          sourceValue = "SYSTEM_TYPE_SAVE";
          break;
        case SaveWithoutMarkerChange:
          sourceValue = "SYSTEM_TYPE_SAVE_WITHOUT_MARKER_CHANGE";
          break;

        default:
          break;
      }
      op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName(), "  return " + sourceValue + ";", true);
    }
    if (op != null) {
      new OperationJob(op).schedule();
    }
  }

}
