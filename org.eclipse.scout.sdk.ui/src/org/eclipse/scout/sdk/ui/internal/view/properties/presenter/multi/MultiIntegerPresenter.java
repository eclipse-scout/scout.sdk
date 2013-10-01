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
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.multi.AbstractMultiValuePresenter;
import org.eclipse.scout.sdk.ui.view.properties.presenter.util.MethodBean;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethodSet;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.scout.sdk.workspace.type.config.parser.IntegerPropertySourceParser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>MultiLongPresenter</h3> ...
 */
public class MultiIntegerPresenter extends AbstractMultiValuePresenter<Integer> {

  public MultiIntegerPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
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
      ConfigurationMethod method = bean.getMethod();
      ConfigPropertyUpdateOperation<Integer> updateOp = new ConfigPropertyUpdateOperation<Integer>(ScoutTypeUtility.getConfigurationMethod(method.getType(), method.getMethodName()), new IntegerPropertySourceParser());
      updateOp.setValue(value);
    }
    new OperationJob(list).schedule();
  }

  private boolean allEqual(Integer[] ar) {
    if (ar.length > 0) {
      Integer ref = ar[0];
      for (Integer b : ar) {
        if (CompareUtility.notEquals(b, ref)) {
          return false;
        }
      }
    }
    return true;
  }
}
