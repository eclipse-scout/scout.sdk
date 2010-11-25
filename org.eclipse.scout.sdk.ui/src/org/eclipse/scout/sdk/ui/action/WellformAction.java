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

import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link WellformAction}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 09.09.2010
 */
public class WellformAction extends OperationAction {

  private final Shell m_shell;

  /**
   * @param label
   * @param imageDescriptor
   * @param operation
   */
  public WellformAction(Shell shell, String label, IOperation operation) {
    super(label, ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_TOOL_LOADING), operation);
    m_shell = shell;
  }

  @Override
  public void run() {
    // user request
    MessageBox box = new MessageBox(m_shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
    box.setMessage("Wellform can take several minutes do you want to continue?");
    if (box.open() == SWT.OK) {
      super.run();
    }
  }

}
