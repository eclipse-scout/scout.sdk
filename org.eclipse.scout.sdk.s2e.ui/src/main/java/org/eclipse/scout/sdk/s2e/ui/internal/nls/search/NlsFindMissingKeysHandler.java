/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
