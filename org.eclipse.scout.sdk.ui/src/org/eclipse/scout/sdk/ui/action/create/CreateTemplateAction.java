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
package org.eclipse.scout.sdk.ui.action.create;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.template.CreateTemplateOperation;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.dialog.TemplateFromFormFieldDialog;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link CreateTemplateAction}</h3>
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 19.09.2010
 */
public class CreateTemplateAction extends AbstractScoutHandler {

  private IType m_type;
  private TemplateFromFormFieldDialog m_dialog;
  private IScoutBundle m_bundle;
  private IPage m_page;

  public CreateTemplateAction() {
    super(Texts.get("CreateTemplate"), null, null, false, Category.TEMPLATE);
  }

  @Override
  public boolean isVisible() {
    return !m_bundle.isBinary();
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    String name = "Abstract" + getType().getElementName();
    m_dialog = new TemplateFromFormFieldDialog(shell, name, getType());
    if (m_dialog.open() == IDialogConstants.OK_ID) {
      CreateTemplateOperation op = new CreateTemplateOperation(getType(), m_bundle);
      op.setTemplateName(m_dialog.getTemplateName());
      op.setPackageName(m_bundle.getPackageName(m_dialog.getTargetPackage()));
      op.setFormDataPackageSuffix(m_dialog.getTargetPackage());
      op.setReplaceFieldWithTemplate(m_dialog.isReplaceFormField());
      op.setCreateExternalFormData(m_dialog.isCreateExternalFormData());
      op.validate();
      OperationJob job = new OperationJob(op);
      job.schedule();
      try {
        job.join();
        m_page.markStructureDirty();
      }
      catch (InterruptedException e) {
        ScoutSdkUi.logWarning("could not wait for job '" + job.getName() + "'.", e);
      }
    }
    return null;
  }

  /**
   * @return the type
   */
  public IType getType() {
    return m_type;
  }

  public void setType(IType type) {
    m_type = type;
    m_bundle = ScoutTypeUtility.getScoutBundle(m_type);
  }

  public void setPage(IPage page) {
    m_page = page;
  }
}
