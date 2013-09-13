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
import org.eclipse.scout.sdk.workspace.type.config.parser.LongPropertySourceParser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>MultiLongPresenter</h3> ...
 */
public class MultiLongPresenter extends AbstractMultiValuePresenter<Long> {

  public MultiLongPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent, "[-+0-9\\'Eeinf]*");
  }

  @Override
  protected void init(ConfigurationMethodSet methodSet) throws CoreException {
    super.init(methodSet);

    MethodBean<Long>[] methodBeans = getMethodBeans();
    long[] ar = new long[methodBeans.length];
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
  protected String formatSourceValue(Long value) throws CoreException {
    if (value.longValue() == Long.MAX_VALUE) {
      return "Long.MAX_VALUE";
    }
    else if (value.longValue() == Long.MIN_VALUE) {
      return "Long.MIN_VALUE";
    }
    return DecimalFormat.getNumberInstance().format(value);
  }

  @Override
  protected String formatDisplayValue(Long value) throws CoreException {
    if (value.longValue() == Long.MAX_VALUE) {
      return SdkProperties.NUMBER_MAX;
    }
    else if (value.longValue() == Long.MIN_VALUE) {
      return SdkProperties.NUMBER_MIN;
    }
    return DecimalFormat.getNumberInstance().format(value);
  }

  @Override
  protected Long parseSourceInput(String input, ConfigurationMethod method) throws CoreException {
    Long d = PropertyMethodSourceUtility.parseReturnParameterLong(input, method.peekMethod(), method.getSuperTypeHierarchy());
    return d;
  }

  @Override
  protected Long parseDisplayInput(String input) throws CoreException {
    Long d = PropertyMethodSourceUtility.parseReturnParameterLong(input);
    return d;
  }

  @Override
  protected synchronized void storeMethods(MethodBean<Long>[] beans, Long value) {
    ArrayList<IOperation> list = new ArrayList<IOperation>();
    for (MethodBean<Long> bean : beans) {
      ConfigurationMethod method = bean.getMethod();
      ConfigPropertyUpdateOperation<Long> updateOp = new ConfigPropertyUpdateOperation<Long>(ScoutTypeUtility.getConfigurationMethod(method.getType(), method.getMethodName()), new LongPropertySourceParser());
      updateOp.setValue(value);
    }
    new OperationJob(list).schedule();
  }

  private boolean allEqual(long[] ar) {
    if (ar.length > 0) {
      long ref = ar[0];
      for (long b : ar) {
        if (b != ref) {
          return false;
        }
      }
    }
    return true;
  }

}
