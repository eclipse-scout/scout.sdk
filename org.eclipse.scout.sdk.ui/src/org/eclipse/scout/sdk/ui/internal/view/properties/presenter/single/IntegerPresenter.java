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

import java.text.DecimalFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractValuePresenter;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.config.parser.IPropertySourceParser;
import org.eclipse.scout.sdk.workspace.type.config.parser.IntegerPropertySourceParser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>IntegerPresenter</h3> ...
 */
public class IntegerPresenter extends AbstractValuePresenter<Integer> {

  private final IPropertySourceParser<Integer> m_parser;

  public IntegerPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent, "[-+0-9\\'eEinf]*");
    m_parser = new IntegerPropertySourceParser();
  }

  public IPropertySourceParser<Integer> getParser() {
    return m_parser;
  }

  @Override
  protected String formatDisplayValue(Integer value) throws CoreException {
    if (value == null) {
      return "";
    }
    else if (value.intValue() == Integer.MAX_VALUE) {
      return SdkProperties.NUMBER_MAX;
    }
    else if (value.intValue() == Integer.MIN_VALUE) {
      return SdkProperties.NUMBER_MIN;
    }
    return DecimalFormat.getIntegerInstance().format(value);
  }

  @Override
  protected Integer parseSourceInput(String input) throws CoreException {
    return parseDisplayInput(input);
  }

  @Override
  protected Integer parseDisplayInput(String input) throws CoreException {
    if (input.equals("")) {
      return getDefaultValue();
    }
    else {
      return getParser().parseSourceValue(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    }
  }

  @Override
  protected synchronized void storeValue(Integer value) throws CoreException {
    try {
      ConfigPropertyUpdateOperation<Integer> updateOp = new ConfigPropertyUpdateOperation<Integer>(getMethod(), getParser());
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
