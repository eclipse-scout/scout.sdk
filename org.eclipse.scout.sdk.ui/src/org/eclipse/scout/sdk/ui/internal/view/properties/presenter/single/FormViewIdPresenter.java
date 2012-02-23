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
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.FormViewIdPresenter.ViewId;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractProposalPresenter;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>FormViewIdPresenter</h3> ...
 */
public class FormViewIdPresenter extends AbstractProposalPresenter<ViewId> {

  public static enum ViewId {
    Outline,
    OutlineSelector,
    PageTable,
    PageDetail,
    PageSearch,
    Editor,
    North,
    NorthEast,
    East,
    SouthEast,
    South,
    SouthWest,
    West,
    NothWest,
    Center
  }

  public FormViewIdPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected void createProposalFieldProviders(ProposalTextField proposalField) {
    ILabelProvider labelProvider = new LabelProvider() {
      @Override
      public String getText(Object element) {
        ViewId value = (ViewId) element;
        switch (value) {
          case OutlineSelector:
        return "Outline Selector";
      case PageTable:
        return "Page Table";
      case PageDetail:
        return "Page Detail";
      case PageSearch:
        return "Page Search";
      case NorthEast:
        return "Noth-East";
      case SouthEast:
        return "South-East";
      case SouthWest:
        return "South-West";
      case NothWest:
        return "Noth-West";
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
    StaticContentProvider provider = new StaticContentProvider(ViewId.values(), labelProvider);
    getProposalField().setContentProvider(provider);
  }

  @Override
  protected ViewId parseInput(String input) throws CoreException {
    String parsedId = PropertyMethodSourceUtility.parseReturnParameterString(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    if (parsedId == null) {
      return null;
    }
    else if (parsedId.equals("OUTLINE")) {
      return ViewId.Outline;
    }
    else if (parsedId.equals("OUTLINE_SELECTOR")) {
      return ViewId.OutlineSelector;
    }
    else if (parsedId.equals("PAGE_TABLE")) {
      return ViewId.PageTable;
    }
    else if (parsedId.equals("PAGE_DETAIL")) {
      return ViewId.PageDetail;
    }
    else if (parsedId.equals("PAGE_SEARCH")) {
      return ViewId.PageSearch;
    }
    else if (parsedId.equals("EDITOR")) {
      return ViewId.Editor;
    }
    else if (parsedId.equals("N")) {
      return ViewId.North;
    }
    else if (parsedId.equals("NE")) {
      return ViewId.NorthEast;
    }
    else if (parsedId.equals("E")) {
      return ViewId.East;
    }
    else if (parsedId.equals("SE")) {
      return ViewId.SouthEast;
    }
    else if (parsedId.equals("S")) {
      return ViewId.South;
    }
    else if (parsedId.equals("SW")) {
      return ViewId.SouthWest;
    }
    else if (parsedId.equals("W")) {
      return ViewId.West;
    }
    else if (parsedId.equals("NW")) {
      return ViewId.NothWest;
    }
    else if (parsedId.equals("C")) {
      return ViewId.Center;
    }
    return null;
  }

  @Override
  protected synchronized void storeValue(ViewId value) {
    if (value == null) {
      value = getDefaultValue();
      getProposalField().acceptProposal(value);
    }
    IOperation op = null;
    if (UiUtility.equals(getDefaultValue(), value)) {
      if (getMethod().isImplemented()) {
        op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
      }
    }
    else {
      StringBuilder source = new StringBuilder();
      switch (value) {
        case Outline:
          source.append("VIEW_ID_OUTLINE");
          break;
        case OutlineSelector:
          source.append("VIEW_ID_OUTLINE_SELECTOR");
          break;
        case PageTable:
          source.append("VIEW_ID_PAGE_TABLE");
          break;
        case PageDetail:
          source.append("VIEW_ID_PAGE_DETAIL");
          break;
        case PageSearch:
          source.append("VIEW_ID_PAGE_SEARCH");
          break;
        case Editor:
          source.append("EDITOR_ID");
          break;
        case North:
          source.append("VIEW_ID_N");
          break;
        case NorthEast:
          source.append("VIEW_ID_NE");
          break;
        case East:
          source.append("VIEW_ID_E");
          break;
        case SouthEast:
          source.append("VIEW_ID_SE");
          break;
        case South:
          source.append("VIEW_ID_S");
          break;
        case SouthWest:
          source.append("VIEW_ID_SW");
          break;
        case West:
          source.append("VIEW_ID_W");
          break;
        case NothWest:
          source.append("VIEW_ID_NW");
          break;
        case Center:
          source.append("VIEW_ID_CENTER");
          break;
      }
      source.append(";");
      op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName(), source.toString(), false);
    }
    if (op != null) {
      new OperationJob(op).schedule();
    }

  }
}
