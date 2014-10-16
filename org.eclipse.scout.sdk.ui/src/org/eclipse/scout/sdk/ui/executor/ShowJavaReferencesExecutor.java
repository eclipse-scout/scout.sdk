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
package org.eclipse.scout.sdk.ui.executor;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.actions.FindReferencesAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.ui.internal.ScoutSdkUi;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

/**
 * <h3>{@link ShowJavaReferencesExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public class ShowJavaReferencesExecutor extends AbstractExecutor {

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    IJavaElement element = UiUtility.adapt(selection.getFirstElement(), IJavaElement.class);
    if (TypeUtility.exists(element)) {
      try {
        IEditorPart part = JavaUI.openInEditor(element, true, true);
        if (part != null) {
          FindReferencesAction a = new FindReferencesAction(part.getEditorSite());
          a.run(element);
        }
      }
      catch (Exception e) {
        ScoutSdkUi.logError(e);
      }
    }
    return null;
  }

  @Override
  public boolean canRun(IStructuredSelection selection) {
    return true;
  }
}
