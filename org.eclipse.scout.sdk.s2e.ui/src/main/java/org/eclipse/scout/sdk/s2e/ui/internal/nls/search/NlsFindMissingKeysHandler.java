/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.search;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.search.ui.NewSearchUI;

/**
 * <h3>{@link NlsFindMissingKeysHandler}</h3>
 *
 * @since 7.0.100
 */
public class NlsFindMissingKeysHandler extends AbstractHandler {
  @Override
  public Object execute(ExecutionEvent event) {
    NewSearchUI.runQueryInBackground(new NlsFindMissingKeysQuery());
    return null;
  }
}
