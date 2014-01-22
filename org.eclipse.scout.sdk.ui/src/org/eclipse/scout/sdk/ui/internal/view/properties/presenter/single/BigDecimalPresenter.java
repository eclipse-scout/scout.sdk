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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractValuePresenter;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.config.parser.BigDecimalPropertySourceParser;
import org.eclipse.scout.sdk.workspace.type.config.parser.IPropertySourceParser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>{@link BigDecimalPresenter}</h3>
 * 
 * @author Matthias Villiger
 * @since 3.10.0 02.12.2013
 */
public class BigDecimalPresenter extends AbstractValuePresenter<BigDecimal> {

  private final IPropertySourceParser<BigDecimal> m_parser;
  private final NumberFormat m_formatter;

  //TODO: fix bigdecmial presenter

  public BigDecimalPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent, "[\\-\\+0-9eE\\.']*");
    m_parser = new BigDecimalPropertySourceParser();
    m_formatter = DecimalFormat.getInstance();
    m_formatter.setMaximumFractionDigits(20);
  }

  public IPropertySourceParser<BigDecimal> getParser() {
    return m_parser;
  }

  @Override
  protected String formatDisplayValue(BigDecimal value) throws CoreException {
    if (value == null) {
      return "";
    }
    return m_formatter.format(value);
  }

  @Override
  protected BigDecimal parseSourceInput(String input) throws CoreException {
    if (input.equals("")) {
      return getDefaultValue();
    }
    else {
      return getParser().parseSourceValue(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    }
  }

  @Override
  protected BigDecimal parseDisplayInput(String input) throws CoreException {
    return parseSourceInput(input.replace("'", ""));
  }

  @Override
  protected synchronized void storeValue(BigDecimal value) throws CoreException {
    try {
      ConfigPropertyUpdateOperation<BigDecimal> updateOp = new ConfigPropertyUpdateOperation<BigDecimal>(getMethod(), getParser());
      updateOp.setValue(value);
      OperationJob job = new OperationJob(updateOp);
      job.setDebug(true);
      job.schedule();
    }
    catch (Exception e) {
      ScoutSdkUi.logError("could not parse default value of method '" + getMethod().getMethodName() + "' in type '" + getMethod().getType().getFullyQualifiedName() + "'.", e);
    }
  }

  @Override
  protected int getTextAlignment() {
    return SWT.RIGHT;
  }
}
