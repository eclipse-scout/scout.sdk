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
package org.eclipse.scout.sdk.ui.internal.view.outline.pages.project.client.form.field;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.form.field.FormFieldNewOperation;
import org.eclipse.scout.sdk.operation.method.MethodOverrideOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IScoutPageConstants;

public class GroupBoxNodePage extends AbstractBoxNodePage {

  public GroupBoxNodePage() {
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_FIELD_GROUP_BOX));
  }

  @Override
  public String getPageId() {
    return IScoutPageConstants.GROUP_BOX_NODE_PAGE;
  }

  @Override
  public void fillContextMenu(IMenuManager manager) {
    super.fillContextMenu(manager);
    manager.add(new Action("Test") {
      @Override
      public void run() {
        new OperationJob(new P_Op(getType())).schedule();
      }
    });
  }

  private class P_Op implements IOperation {
    private final IType m_declaringType;

    public P_Op(IType declaringType) {
      m_declaringType = declaringType;

    }

    @Override
    public String getOperationName() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
      ScoutSdk.logInfo("run operation: [" + getOperationName() + "]");
      FormFieldNewOperation newOp = new FormFieldNewOperation(m_declaringType);
      newOp.setTypeName("Blubber");
      newOp.setSuperTypeSignature(Signature.createTypeSignature("com.bsiag.miniapp.client.ui.fields.AbstractTestField", true));
      newOp.setSiblingField(null);
      newOp.validate();
      newOp.run(monitor, workingCopyManager);
      IType createdField = newOp.getCreatedFormField();
      MethodOverrideOperation op = new MethodOverrideOperation(createdField, "getConfiguredTest");
      op.setSimpleBody("return null;");
      op.run(monitor, workingCopyManager);

    }

    @Override
    public void validate() throws IllegalArgumentException {
      // TODO Auto-generated method stub

    }
  }
}
