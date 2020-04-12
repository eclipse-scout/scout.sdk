/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.action;

import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.search.NlsFindKeyQuery;
import org.eclipse.search.ui.NewSearchUI;

/**
 * <h4>FindReferencesAction</h4>
 */
public class FindReferencesAction extends Action {

  private final TranslationStoreStack m_project;
  private final String m_key;

  public FindReferencesAction(TranslationStoreStack project, String key) {
    super("Find References to '" + key + '\'');
    m_project = project;
    m_key = key;
    setImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.Find));
  }

  @Override
  public void run() {
    NewSearchUI.runQueryInBackground(new NlsFindKeyQuery(m_project, m_key));
  }
}
