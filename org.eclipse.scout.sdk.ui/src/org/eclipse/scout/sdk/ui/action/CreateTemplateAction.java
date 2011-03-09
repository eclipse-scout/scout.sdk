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
package org.eclipse.scout.sdk.ui.action;

import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.template.CreateTemplateOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.dialog.TemplateFromFromFieldDialog;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.SdkTypeUtility;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link CreateTemplateAction}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 19.09.2010
 */
public class CreateTemplateAction extends Action {

  private final IType m_type;
  private TemplateFromFromFieldDialog m_dialog;
  private final Shell m_shell;
  private IScoutBundle m_bundle;
  private String m_packageName;
  private final IPage m_page;

  public CreateTemplateAction(Shell shell, IPage page, IType type) {
    super("Create template...");
    m_shell = shell;
    m_page = page;
    m_type = type;
    m_bundle = SdkTypeUtility.getScoutBundle(m_type);
    m_packageName = m_bundle.getPackageName(IScoutBundle.CLIENT_PACKAGE_APPENDIX_UI_TEMPLATE_FORM_FIELD);
  }

  @Override
  public void run() {
    String name = "Abstract" + getType().getElementName();
    m_dialog = new TemplateFromFromFieldDialog(getShell(), name, getType());
    if (m_dialog.open() == IDialogConstants.OK_ID) {
      CreateTemplateOperation op = new CreateTemplateOperation(getType());
      op.setTemplateName(m_dialog.getTemplateName());
      op.setPackageName(m_packageName);
      op.setTemplateBundle(m_bundle);
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
  }

  /**
   * @return the shell
   */
  public Shell getShell() {
    return m_shell;
  }

  /**
   * @return the type
   */
  public IType getType() {
    return m_type;
  }

}
