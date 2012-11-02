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
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractValuePresenter;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>DoublePresenter</h3> ...
 */
public class DoublePresenter extends AbstractValuePresenter<Double> {

  public DoublePresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent, "[-+0-9\\.\\,\\'eEinfd]*");
  }

  @Override
  protected String formatSourceValue(Double value) throws CoreException {
    if (value == null) {
      return "null";
    }
    else if (value.doubleValue() == Double.MAX_VALUE) {
      return "Double.MAX_VALUE";
    }
    else if (value.doubleValue() == -Double.MAX_VALUE) {
      return "-Double.MAX_VALUE";
    }
    String sourceVal = value.toString();
    return sourceVal;
  }

  @Override
  protected String formatDisplayValue(Double value) throws CoreException {
    if (value == null) {
      return "";
    }
    else if (value.doubleValue() == Double.MAX_VALUE) {
      return SdkProperties.NUMBER_MAX;
    }
    else if (value.doubleValue() == -Double.MAX_VALUE) {
      return SdkProperties.NUMBER_MIN;
    }
    return DecimalFormat.getInstance().format(value);
  }

  @Override
  protected Double parseSourceInput(String input) throws CoreException {
    return parseDisplayInput(input);
  }

  @Override
  protected Double parseDisplayInput(String input) throws CoreException {
    if (input.equals("")) {
      return getDefaultValue();
    }
    Double d = PropertyMethodSourceUtility.parseReturnParameterDouble(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    return d;
  }

  @Override
  protected synchronized void storeValue(Double value) throws CoreException {
    IOperation op = null;
    if (UiUtility.equals(getDefaultValue(), value)) {
      if (getMethod().isImplemented()) {
        op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
      }
    }
    else {
      String sourceValue = formatSourceValue(value);
      op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName(), "  return " + sourceValue + ";", true);
    }
    if (op != null) {
      new OperationJob(op).schedule();
    }
  }

  @Override
  protected int getTextAlignment() {
    return SWT.RIGHT;
  }

}
