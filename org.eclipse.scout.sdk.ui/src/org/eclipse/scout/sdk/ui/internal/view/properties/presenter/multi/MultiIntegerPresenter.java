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
package org.eclipse.scout.sdk.ui.internal.view.properties.presenter.multi;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.properties.presenter.multi.AbstractMultiValuePresenter;
import org.eclipse.scout.sdk.ui.view.properties.presenter.util.MethodBean;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethodSet;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>MultiLongPresenter</h3> ...
 */
public class MultiIntegerPresenter extends AbstractMultiValuePresenter<Integer> {

  public MultiIntegerPresenter(FormToolkit toolkit, Composite parent) {
    super(toolkit, parent, "[-+0-9\\'eEinf]*");
  }

  @Override
  protected void init(ConfigurationMethodSet methodSet) throws CoreException {
    super.init(methodSet);
    MethodBean<Integer>[] methodBeans = getMethodBeans();
    Integer[] ar = new Integer[methodBeans.length];
    for (int i = 0; i < methodBeans.length; i++) {
      ar[i] = methodBeans[i].getCurrentSourceValue();
    }
    if (!allEqual(ar)) {
      getTextComponent().setText(SdkProperties.INPUT_MULTI_UNDEFINED);
    }
    else {
      getTextComponent().setText(formatDisplayValue(ar[0]));
    }
  }

  @Override
  protected int getTextAlignment() {
    return SWT.RIGHT;
  }

  @Override
  protected String formatSourceValue(Integer value) throws CoreException {
    if (value == null) {
      return "null";
    }
    if (value.intValue() == Integer.MAX_VALUE) {
      return "Integer.MAX_VALUE";
    }
    else if (value.intValue() == -Integer.MAX_VALUE) {
      return "-Integer.MAX_VALUE";
    }
    return DecimalFormat.getNumberInstance().format(value);
  }

  @Override
  protected String formatDisplayValue(Integer value) throws CoreException {
    if (value == null) {
      return "";
    }
    if (value.intValue() == Integer.MAX_VALUE) {
      return SdkProperties.NUMBER_MAX;
    }
    else if (value.intValue() == -Integer.MAX_VALUE) {
      return SdkProperties.NUMBER_MIN;
    }
    return DecimalFormat.getNumberInstance().format(value);
  }

  @Override
  protected Integer parseSourceInput(String value, ConfigurationMethod method) throws CoreException {
    Integer d = PropertyMethodSourceUtility.parseReturnParameterInteger(value, method.peekMethod(), method.getSuperTypeHierarchy());
    return d;
  }

  @Override
  protected Integer parseDisplayInput(String input) throws CoreException {

    Integer d = PropertyMethodSourceUtility.parseReturnParameterInteger(input);
    return d;
  }

  @Override
  protected synchronized void storeMethods(MethodBean<Integer>[] beans, Integer value) {
    ArrayList<IOperation> list = new ArrayList<IOperation>();
    for (MethodBean<Integer> bean : beans) {
      try {
        String sourceValue = formatSourceValue(value);
        ConfigurationMethod method = bean.getMethod();
        if (UiUtility.equals(method.computeDefaultValue(), sourceValue)) {
          if (method.isImplemented()) {
            list.add(new ScoutMethodDeleteOperation(method.peekMethod()));
          }
        }
        else {
          list.add(new ConfigPropertyMethodUpdateOperation(method.getType(), method.getMethodName(), "return " + sourceValue + ";", true));
        }
      }
      catch (CoreException e) {
        ScoutSdkUi.logError("could not format source value", e);
      }
    }
    new OperationJob(list).schedule();
  }

  private boolean allEqual(Integer[] ar) {
    if (ar.length > 0) {
      Integer ref = ar[0];
      for (Integer b : ar) {
        if (b == null && ref != null) {
          return false;
        }
        if (b != null && ref == null) {
          return false;
        }
        if (b != ref) {
          return false;
        }
      }
    }
    return true;
  }

}
