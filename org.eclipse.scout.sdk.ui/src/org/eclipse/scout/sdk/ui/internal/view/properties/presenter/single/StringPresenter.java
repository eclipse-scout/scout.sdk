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
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractValuePresenter;
import org.eclipse.scout.sdk.workspace.type.config.ConfigPropertyUpdateOperation;
import org.eclipse.scout.sdk.workspace.type.config.parser.IPropertySourceParser;
import org.eclipse.scout.sdk.workspace.type.config.parser.StringPropertySourceParser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * <h3>StringPresenter</h3> Representing a plain text property method.
 * References like 'm_value' or 'IConstants.ASTRING' are handled.
 */
public class StringPresenter extends AbstractValuePresenter<String> {

  private final IPropertySourceParser<String> m_parser;

  public StringPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent, null);
    m_parser = new StringPropertySourceParser();
  }

  public IPropertySourceParser<String> getParser() {
    return m_parser;
  }

  @Override
  protected String formatDisplayValue(String value) throws CoreException {
    return value;
  }

  @Override
  protected String parseSourceInput(String input) throws CoreException {
    return getParser().parseSourceValue(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
  }

  @Override
  protected String parseDisplayInput(String input) throws CoreException {
    if (StringUtility.isNullOrEmpty(input)) {
      return null;
    }
    return input;

  }

  @Override
  protected synchronized void storeValue(String value) throws CoreException {
    try {
      ConfigPropertyUpdateOperation<String> updateOp = new ConfigPropertyUpdateOperation<String>(getMethod(), getParser());
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
    return SWT.LEFT;
  }
}
