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
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractValuePresenter;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>StringPresenter</h3> Representing a plain text property method.
 * References like 'm_value' or 'IConstants.ASTRING' are handled.
 */
public class StringPresenter extends AbstractValuePresenter<String> {

  public StringPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent, null);
  }

  @Override
  protected String formatSourceValue(String value) throws CoreException {
    return JdtUtility.toStringLiteral(value);
  }

  @Override
  protected String formatDisplayValue(String value) throws CoreException {
    return value;
  }

  @Override
  protected String parseSourceInput(String input) throws CoreException {
    String value = PropertyMethodSourceUtility.parseReturnParameterString(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    if (value == null) {
      value = "";
    }
    return value;
  }

  @Override
  protected String parseDisplayInput(String input) throws CoreException {
    return input;
  }

  @Override
  protected synchronized void storeValue(String value) throws CoreException {
    IOperation op = null;
    if (UiUtility.equals(getDefaultValue(), value)) {
      if (getMethod().isImplemented()) {
        op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
      }
    }
    else {
      String sourceValue = formatSourceValue(value);
      op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName(), "  return " + sourceValue + ";", false);
    }
    if (op != null) {
      new OperationJob(op).schedule();
    }
  }

}
