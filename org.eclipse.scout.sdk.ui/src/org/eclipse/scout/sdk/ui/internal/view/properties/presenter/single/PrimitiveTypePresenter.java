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

import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.internal.fields.proposal.PrimitiveTypeProposal;
import org.eclipse.scout.sdk.ui.view.properties.presenter.single.AbstractProposalPresenter;
import org.eclipse.scout.sdk.workspace.type.config.ConfigurationMethod;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtilities;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * <h3>PrimitiveTypePresenter</h3> A proposal presenter which allows to select a primitive wrapper type or
 * String
 */
public class PrimitiveTypePresenter extends AbstractProposalPresenter<PrimitiveTypeProposal> {

  private static final Class<?>[] PRIMITIVE_TYPES = new Class<?>[]{String.class, Double.class, Float.class, Long.class, Integer.class, Short.class,
      Date.class, Byte.class};

  public PrimitiveTypePresenter(FormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  /**
   * Loads proposals for all types defined in <code>PRIMITIVE_TYPES</code>
   */
  @Override
  protected void init(ConfigurationMethod method) throws CoreException {
    PrimitiveTypeProposal[] proposals = new PrimitiveTypeProposal[PRIMITIVE_TYPES.length];

    for (int i = 0; i < proposals.length; i++) {
      proposals[i] = new PrimitiveTypeProposal(ScoutSdkUi.getImage(ScoutSdkUi.IMG_DEFAULT), ScoutSdk.getType(PRIMITIVE_TYPES[i].getName()));
    }
    setProposals(proposals);

    super.init(method);
  }

  @Override
  protected PrimitiveTypeProposal parseInput(String input) throws CoreException {
    IType referedType = PropertyMethodSourceUtilities.parseReturnParameterClass(input, getMethod().peekMethod());
    return findProposal(referedType);
  }

  private PrimitiveTypeProposal findProposal(IType referedType) {
    for (PrimitiveTypeProposal prop : getProposals()) {
      if (CompareUtility.equals(prop.getPrimitiveType(), referedType)) {
        return prop;
      }
    }
    return null;
  }

  @Override
  protected synchronized void storeValue(PrimitiveTypeProposal value) {
    IOperation op = null;
    if (ScoutSdkUtility.equals(getDefaultValue(), value)) {
      if (getMethod().isImplemented()) {
        op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
      }
    }
    else {
      String sourceValue = value.getPrimitiveType().getElementName() + ".class";
      op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName(), "  return " + sourceValue + ";", false);
    }
    if (op != null) {
      new OperationJob(op).schedule();
    }
  }

}
