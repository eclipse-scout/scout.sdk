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
package org.eclipse.scout.sdk.ui.action.delete;

import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.util.TypeDeleteOperation;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class DeleteAction extends AbstractScoutHandler {
  private String m_name;
  private ArrayList<IType> m_types;

  public DeleteAction() {
    super(Texts.get("DeleteWithPopup"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolRemove), "Delete", true, Category.DELETE);
    m_types = new ArrayList<IType>();
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
    box.setText(Texts.get("Question"));
    if (m_types.size() == 1) {
      if (!StringUtility.hasText(m_name) && m_types.get(0) != null) {
        m_name = m_types.get(0).getElementName();
      }
      box.setMessage(Texts.get("DeleteAction_ensureRequest", m_name));
    }
    else {
      box.setMessage(Texts.get("DeleteAction_ensureRequestPlural"));
    }
    if (box.open() == SWT.OK) {
      for (IType t : m_types) {
        OperationJob job = new OperationJob(new TypeDeleteOperation(t));
        job.schedule();
      }
    }
    return null;
  }

  public void addType(IType t) {
    m_types.add(t);
  }

  public void setName(String name) {
    m_name = name;
  }
}
