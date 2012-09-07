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
package org.eclipse.scout.sdk.ui.view.properties.presenter.single;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.ConfigPropertyMethodUpdateOperation;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.method.ScoutMethodDeleteOperation;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.ui.view.properties.PropertyViewFormToolkit;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.workspace.type.config.PropertyMethodSourceUtility;
import org.eclipse.swt.widgets.Composite;

public abstract class AbstractTypeProposalPresenter extends AbstractProposalPresenter<IType> {

  public AbstractTypeProposalPresenter(PropertyViewFormToolkit toolkit, Composite parent) {
    super(toolkit, parent);
  }

  @Override
  protected IType parseInput(String input) throws CoreException {
    IType referedType = PropertyMethodSourceUtility.parseReturnParameterClass(input, getMethod().peekMethod());
    return referedType;
  }

  @Override
  protected synchronized void storeValue(final IType value) {
    IOperation op = null;
    if (UiUtility.equals(getDefaultValue(), value)) {
      if (getMethod().isImplemented()) {
        op = new ScoutMethodDeleteOperation(getMethod().peekMethod());
      }
    }
    else {
      op = new ConfigPropertyMethodUpdateOperation(getMethod().getType(), getMethod().getMethodName()) {
        @Override
        protected String createMethodBody(IMethod methodToOverride, IImportValidator validator) throws JavaModelException {
          StringBuilder source = new StringBuilder();
          source.append("  return ");
          if (value != null) {
            source.append(SignatureUtility.getTypeReference(Signature.createTypeSignature(value.getFullyQualifiedName(), true), validator) + ".class;");
          }
          else {
            source.append("null;");
          }
          return source.toString();
        }
      };
      ((ConfigPropertyMethodUpdateOperation) op).setFormatSource(true);
    }
    new OperationJob(op).schedule();
  }

  @Override
  protected void createContextMenu(MenuManager manager) {
    super.createContextMenu(manager);
    if (getCurrentSourceValue() != null) {
      final IType t = getCurrentSourceValue();
      if (t != null) {
        manager.add(new Action(Texts.get("GoTo") + t.getElementName(), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.StatusInfo)) {
          @Override
          public void run() {
            showJavaElementInEditor(t);
          }
        });
      }
    }
  }

}
