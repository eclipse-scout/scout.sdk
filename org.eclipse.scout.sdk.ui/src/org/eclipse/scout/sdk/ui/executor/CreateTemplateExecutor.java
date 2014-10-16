/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.executor;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.template.CreateTemplateOperation;
import org.eclipse.scout.sdk.ui.dialog.TemplateFromFormFieldDialog;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link CreateTemplateExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class CreateTemplateExecutor extends AbstractExecutor {

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    IType type = UiUtility.getTypeFromSelection(selection);
    String name = "Abstract" + type.getElementName();
    TemplateFromFormFieldDialog dialog = new TemplateFromFormFieldDialog(shell, name, type);
    if (dialog.open() == IDialogConstants.OK_ID) {
      IScoutBundle bundle = ScoutTypeUtility.getScoutBundle(type);
      CreateTemplateOperation op = new CreateTemplateOperation(type, bundle);
      op.setTemplateName(dialog.getTemplateName());
      op.setPackageName(bundle.getPackageName(dialog.getTargetPackage()));
      op.setFormDataPackageSuffix(dialog.getTargetPackage());
      op.setReplaceFieldWithTemplate(dialog.isReplaceFormField());
      op.setCreateExternalFormData(dialog.isCreateExternalFormData());
      op.validate();

      new OperationJob(op).schedule();
    }
    return null;
  }

  @Override
  public boolean canRun(IStructuredSelection selection) {
    return isEditable(UiUtility.getTypeFromSelection(selection));
  }

}
