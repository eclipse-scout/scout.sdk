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
import org.eclipse.jdt.ui.actions.FindReferencesAction;
import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.ui.IEditorPart;

public class ShowJavaReferencesAction extends Action {
  private IJavaElement m_element;

  public ShowJavaReferencesAction(IJavaElement e) {
    super(Texts.get("Process_findReferencesX", e.getElementName()), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.IMG_FIND));
    m_element = e;
  }

  @Override
  public void run() {
    if (m_element != null && m_element.exists()) {
      try {
        IEditorPart part = JavaUI.openInEditor(m_element, true, true);
        if (part != null) {
          FindReferencesAction a = new FindReferencesAction(part.getEditorSite());
          a.run(m_element);
        }
      }
      catch (Exception e) {
        ScoutSdkUi.logError(e);
      }
    }
  }
}
