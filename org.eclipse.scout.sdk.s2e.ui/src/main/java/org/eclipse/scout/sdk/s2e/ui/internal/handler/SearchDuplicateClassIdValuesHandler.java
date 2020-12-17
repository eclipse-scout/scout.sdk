/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.scout.sdk.s2e.classid.ClassIdValidationJob;

/**
 * <h3>{@link SearchDuplicateClassIdValuesHandler}</h3>
 *
 * @since 8.0.0
 */
public class SearchDuplicateClassIdValuesHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) {
    ClassIdValidationJob.executeAsync(0L, true);
    return null;
  }
}
