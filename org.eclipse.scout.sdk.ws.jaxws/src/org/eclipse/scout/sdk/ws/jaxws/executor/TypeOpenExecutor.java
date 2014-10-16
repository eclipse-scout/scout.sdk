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
package org.eclipse.scout.sdk.ws.jaxws.executor;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scout.sdk.ui.executor.AbstractExecutor;
import org.eclipse.scout.sdk.ui.util.UiUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.ws.jaxws.JaxWsSdk;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>{@link TypeOpenExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 14.10.2014
 */
public class TypeOpenExecutor extends AbstractExecutor {

  @Override
  public boolean canRun(IStructuredSelection selection) {
    return true;
  }

  @Override
  public Object run(Shell shell, IStructuredSelection selection, ExecutionEvent event) {
    IType type = UiUtility.getTypeFromSelection(selection);
    if (!TypeUtility.exists(type)) {
      return null;
    }

    try {
      JavaUI.openInEditor(type);
    }
    catch (Exception e) {
      JaxWsSdk.logWarning(e);
    }
    return null;
  }

}
