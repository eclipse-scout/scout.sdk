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
import org.eclipse.scout.sdk.ui.fields.proposal.ScoutProposalUtility;
import org.eclipse.scout.sdk.ui.internal.fields.proposal.ConstantFieldProposal;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractProposalPresenter;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>HorizontalAglinmentPresenter</h3> ...
 */
public class HorizontalAlignmentPresenter extends AbstractProposalPresenter<ConstantFieldProposal<Integer>> {

  private static final int LEFT = -1;
  private static final int CENTER = 0;
  private static final int RIGHT = 1;

  public HorizontalAlignmentPresenter(FormToolkit toolkit, Composite parent) {
    super(toolkit, parent);

  }

  @Override
  protected void init(ConfigurationMethod method) throws CoreException {
    setProposals(ScoutProposalUtility.getHorizontalAlignmentProposals());
    super.init(method);
  }

  @Override
  protected ConstantFieldProposal<Integer> parseInput(String input) throws CoreException {
    int parsedInt = PropertyMethodSourceUtility.parseReturnParameterInteger(input, getMethod().peekMethod(), getMethod().getSuperTypeHierarchy());
    return findProposal(parsedInt);
  }

  @Override
  protected synchronized void storeValue(ConstantFieldProposal<Integer> value) {
    if (value == null) {
      // set to default
      getProposalComponent().acceptProposal(getDefaultValue());
      value = getDefaultValue();
    }
    //IField field = value.getField();
    IOperation op = null;
    if (UiUtility.equals(getDefaultValue(), value)) {
      if (getMethod().isImplemented()) {
        op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
      }
    }
    else {
      String sourceValue = "" + value.getConstantValue();
      op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName(), "  return " + sourceValue + ";", false);
    }
    if (op != null) {
      OperationJob job = new OperationJob(op);
      job.schedule();
//      try {
//        job.join();
//        if (op instanceof ConfigPropertyMethodUpdateOperation) {
//          IMethod m = ((ConfigPropertyMethodUpdateOperation) op).getUpdatedMethod();
//          if (TypeUtility.exists(m)) {
//            showJavaElementInEditor(m, false);
//          }
//        }
//      }
//      catch (Exception e) {
//        ScoutSdkUi.logWarning("job '" + job.getName() + "' interruppted.", e);
//      }
    }
  }

  private ConstantFieldProposal<Integer> findProposal(int id) {
    if (id > 0) {
      id = RIGHT;
    }
    else if (id == 0) {
      id = CENTER;
    }
    else {
      id = LEFT;
    }
    for (ConstantFieldProposal<Integer> prop : getProposals()) {
      if (prop.getConstantValue() == id) {
        return prop;
      }
    }
    return null;
  }

}
