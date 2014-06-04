/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ws.jaxws.swt.dialog;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.sdk.ui.wizard.ScoutWizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

public class ScoutWizardDialogEx extends ScoutWizardDialog {

  private Integer m_width;
  private Integer m_height;

  public ScoutWizardDialogEx(IWizard newWizard) {
    super(newWizard);
  }

  public ScoutWizardDialogEx(Shell parentShell, IWizard newWizard) {
    super(parentShell, newWizard);
  }

  @Override
  protected void configureShell(Shell newShell) {
    Point pt = newShell.getDisplay().getCursorLocation();

    int width = NumberUtility.nvl(m_width, newShell.getBounds().width);
    int height = NumberUtility.nvl(m_height, newShell.getBounds().height);

    newShell.setLocation(pt.x - width / 2, pt.y - height / 2);
    newShell.setSize(width, height);
    super.configureShell(newShell);
  }

  public void setWidth(int width) {
    m_width = width;
  }

  public void setHeight(int height) {
    m_height = height;
  }
}
