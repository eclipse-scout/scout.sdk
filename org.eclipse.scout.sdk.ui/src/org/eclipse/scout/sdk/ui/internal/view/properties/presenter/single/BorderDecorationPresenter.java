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
import org.eclipse.scout.sdk.ui.internal.view.properties.presenter.single.BorderDecorationPresenter.BorderDecoration;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractProposalPresenter;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>{@link BorderDecorationPresenter}</h3> ...
 * 
 * @author mvi
 * @since 3.8.0 04.12.2012
 */
public class BorderDecorationPresenter extends AbstractProposalPresenter<BorderDecoration> {

  private final static String BORDER_DECORATION_EMPTY = "empty";
  private final static String BORDER_DECORATION_LINE = "line";
  private final static String BORDER_DECORATION_SECTION = "section";
  private final static String BORDER_DECORATION_AUTO = "auto";

  protected static enum BorderDecoration {
    Empty,
    Line,
    Section,
    Auto
  }

  public BorderDecorationPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
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
        return ScoutSdkUi.getImage(ScoutSdkUi.Default);
      }

    };
    getProposalField().setLabelProvider(labelProvider);
    StaticContentProvider provider = new StaticContentProvider(BorderDecoration.values(), labelProvider);
    getProposalField().setContentProvider(provider);
  }

  @Override
  protected BorderDecoration parseInput(String input) throws CoreException {
    String parsedString = PropertyMethodSourceUtility.parseReturnParameterString(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    if (BORDER_DECORATION_EMPTY.equals(parsedString)) {
      return BorderDecoration.Empty;
    }
    else if (BORDER_DECORATION_LINE.equals(parsedString)) {
      return BorderDecoration.Line;
    }
    else if (BORDER_DECORATION_SECTION.equals(parsedString)) {
      return BorderDecoration.Section;
    }
    else if (BORDER_DECORATION_AUTO.equals(parsedString)) {
      return BorderDecoration.Auto;
    }
    return null;
  }

  @Override
  protected synchronized void storeValue(BorderDecoration value) throws CoreException {
    if (value == null) {
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
        case Empty:
          source.append("BORDER_DECORATION_EMPTY");
          break;
        case Line:
          source.append("BORDER_DECORATION_LINE");
          break;
        case Section:
          source.append("BORDER_DECORATION_SECTION");
          break;
        case Auto:
          source.append("BORDER_DECORATION_AUTO");
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
