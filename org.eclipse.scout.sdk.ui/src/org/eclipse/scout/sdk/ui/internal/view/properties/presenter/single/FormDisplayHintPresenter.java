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
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
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
 * <h3>FormDisplayHintPresenter</h3> ...
 */
public class FormDisplayHintPresenter extends AbstractProposalPresenter<ConstantFieldProposal<Integer>> {

  public FormDisplayHintPresenter(FormToolkit toolkit, Composite parent) {
    super(toolkit, parent);

  }

  @Override
  protected ConstantFieldProposal<Integer> parseInput(String input) throws CoreException {
    int parsedInt = PropertyMethodSourceUtilities.parseReturnParameterInteger(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    return findProposal(parsedInt);
  }

  @Override
  protected void init(ConfigurationMethod method) throws CoreException {
    setProposals(ScoutProposalUtility.getFormDisplayHintProposals());
    super.init(method);
  }

  @Override
  protected synchronized void storeValue(ConstantFieldProposal<Integer> value) {
    if (value == null) {
      getProposalComponent().acceptProposal(getDefaultValue());
      value = getDefaultValue();
    }
    IOperation op = null;
    if (ScoutSdkUtility.equals(getDefaultValue(), value)) {
      if (getMethod().isImplemented()) {
        op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
      }
    }
    else {
      final IField finalField = value.getField();
      op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName()) {
        @Override
        protected String createMethodBody(IMethod methodToOverride, IImportValidator validator) throws JavaModelException {
          return "  return " + finalField.getElementName() + ";";
        }

      };
    }
    if (op != null) {
      new OperationJob(op).schedule();
    }
  }

  private ConstantFieldProposal<Integer> findProposal(int id) {
    for (ConstantFieldProposal<Integer> prop : getProposals()) {
      if (prop.getConstantValue() == id) {
        return prop;
      }
    }
    return null;
  }

}
