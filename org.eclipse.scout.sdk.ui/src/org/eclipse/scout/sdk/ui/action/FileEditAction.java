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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class FileEditAction extends Action {
  private IFile m_file;

  public FileEditAction(IFile f, ImageDescriptor icon) {
    super(Texts.get("Edit") + " " + f.getParent().getName() + "/" + f.getName(), icon);
    m_file = f;
  }

  @Override
  public void run() {
    try {
      IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), m_file, true);
    }
    catch (Exception e) {
      ScoutSdkUi.logError(e);
    }
  }
}
