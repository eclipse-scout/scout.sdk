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
package org.eclipse.scout.sdk.ui.action.create;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.scout.sdk.RuntimeClasses;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.jobs.OperationJob;
import org.eclipse.scout.sdk.operation.util.ClassIdNewOperation;
import org.eclipse.scout.sdk.ui.action.AbstractScoutHandler;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link ClassIdNewAction}</h3>
 *
 * @author Matthias Villiger
 * @since 3.10.0 05.01.2014
 */
public class ClassIdNewAction extends AbstractScoutHandler {

  private IScoutBundle m_startBundle;

  public ClassIdNewAction() {
    super(Texts.get("CreateMissingClassIdAnnotations"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ServiceLocatorAdd), null, false, Category.NEW);
  }

  @Override
  public boolean isVisible() {
    return TypeUtility.existsType(RuntimeClasses.ITypeWithClassId);
  }

  public void setScoutBundle(IScoutBundle sb) {
    m_startBundle = sb;
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
    MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
    box.setMessage(Texts.get("LongRunningOpConfirmationMsg"));
    if (box.open() == SWT.OK) {
      ClassIdNewOperation cno = new ClassIdNewOperation(m_startBundle);
      new OperationJob(cno).schedule();
    }
    return null;
  }
}
