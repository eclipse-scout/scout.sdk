/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
