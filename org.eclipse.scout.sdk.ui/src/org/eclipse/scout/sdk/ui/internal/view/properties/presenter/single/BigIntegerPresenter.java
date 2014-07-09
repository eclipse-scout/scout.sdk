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

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractValuePresenter;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.config.parser.BigIntegerPropertySourceParser;
import org.eclipse.scout.sdk.workspace.type.config.parser.IPropertySourceParser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>{@link BigIntegerPresenter}</h3>
 *
 * @author Matthias Villiger
 * @since 3.10.0 02.12.2013
 */
public class BigIntegerPresenter extends AbstractValuePresenter<BigInteger> {

  private final IPropertySourceParser<BigInteger> m_parser;

  public BigIntegerPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent, "[\\-\\+0-9]*");
    m_parser = new BigIntegerPropertySourceParser();
  }

  public IPropertySourceParser<BigInteger> getParser() {
    return m_parser;
  }

  @Override
  protected String formatDisplayValue(BigInteger value) throws CoreException {
    if (value == null) {
      return "";
    }
    NumberFormat formatter = DecimalFormat.getNumberInstance(Locale.ENGLISH);
    formatter.setGroupingUsed(false);
    return formatter.format(value);
  }

  @Override
  protected BigInteger parseSourceInput(String input) throws CoreException {
    if (input.equals("")) {
      return getDefaultValue();
    }
    else {
      return getParser().parseSourceValue(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    }
  }

  @Override
  protected BigInteger parseDisplayInput(String input) throws CoreException {
    return parseSourceInput(input.replace("'", ""));
  }

  @Override
  protected synchronized void storeValue(BigInteger value) throws CoreException {
    try {
      ConfigPropertyUpdateOperation<BigInteger> updateOp = new ConfigPropertyUpdateOperation<BigInteger>(getMethod(), getParser());
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
