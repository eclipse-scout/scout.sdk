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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;

/**
 * <h3>OpenSourceEditorAction</h3> ...
 */
public class OpenSourceEditorAction extends Action {
  private final IJavaElement m_element;

  public OpenSourceEditorAction(String name, IJavaElement element) {
    m_element = element;
    setText(name);
    setImageDescriptor(ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_SOURCE_FILE));
  }

  @Override
  public void run() {
    try {
      JavaUI.openInEditor(m_element);
    }
    catch (Exception e) {
      ScoutSdkUi.logWarning(e);
    }
  }

}
