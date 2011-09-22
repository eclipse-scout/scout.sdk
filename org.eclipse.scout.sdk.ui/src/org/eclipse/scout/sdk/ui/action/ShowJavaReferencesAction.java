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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.actions.FindReferencesAction;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.view.outline.pages.IPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

public class ShowJavaReferencesAction extends AbstractScoutHandler {
  private IJavaElement m_element;

  public void setElement(IJavaElement element) {
    m_element = element;
  }

  public ShowJavaReferencesAction() {
    super(Texts.get("FindJavaReferences"), ScoutSdkUi.getImageDescriptor(ScoutSdkUi.ToolSearch));
  }

  @Override
  public Object execute(Shell shell, IPage[] selection, ExecutionEvent event) throws ExecutionException {
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
    return null;
  }
}
