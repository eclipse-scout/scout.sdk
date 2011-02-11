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
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.fields.proposal.ScoutProposalUtility;
import org.eclipse.scout.sdk.ui.internal.fields.proposal.ConstantFieldProposal;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractProposalPresenter;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtilities;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>FormViewIdPresenter</h3> ...
 */
public class FormViewIdPresenter extends AbstractProposalPresenter<ConstantFieldProposal<String>> {

  public FormViewIdPresenter(FormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected void init(ConfigurationMethod method) throws CoreException {
    setProposals(ScoutProposalUtility.getFormViewIdTypeProposals());
    super.init(method);
  }

  @Override
  protected ConstantFieldProposal<String> parseInput(String input) throws CoreException {
    String parsedId = PropertyMethodSourceUtilities.parseReturnParameterString(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    return findProposal(parsedId);
  }

  @Override
  protected synchronized void storeValue(ConstantFieldProposal<String> value) {
    if (value == null) {
      value = getDefaultValue();
      getProposalComponent().acceptProposal(value);
    }
    IOperation op = null;
    if (ScoutSdkUtility.equals(getDefaultValue(), value)) {
      if (getMethod().isImplemented()) {
        op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
      }
    }
    else {
      String sourceValue = value.getField().getElementName();
      op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName(), "  return " + sourceValue + ";", false);
    }
    if (op != null) {
      new OperationJob(op).schedule();
    }

  }

  private ConstantFieldProposal<String> findProposal(String id) {
    for (ConstantFieldProposal<String> prop : getProposals()) {
      if (prop.getConstantValue().equals(id)) {
        return prop;
      }
    }
    return null;
  }

}
