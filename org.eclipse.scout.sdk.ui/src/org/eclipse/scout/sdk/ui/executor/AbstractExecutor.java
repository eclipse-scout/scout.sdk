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

import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.scout.sdk.ui.extensions.executor.IExecutor;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.IScoutBundle;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 * <h3>{@link AbstractExecutor}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 13.10.2014
 */
public abstract class AbstractExecutor implements IExecutor {

  protected boolean isEditable(IScoutBundle b) {
    return b != null && !b.isBinary();
  }

  protected boolean isEditable(IJavaElement element) {
    if (!TypeUtility.exists(element)) {
      return false;
    }
    if (element.isReadOnly()) {
      return false;
    }
    IScoutBundle b = ScoutTypeUtility.getScoutBundle(element);
    return b != null && !b.isBinary();
  }

  protected boolean isEditable(Set<? extends IJavaElement> types) {
    if (types == null || types.isEmpty()) {
      return false;
    }

    for (IJavaElement typeToDelete : types) {
      if (!isEditable(typeToDelete)) {
        return false;
      }
    }
    return true;
  }
}
