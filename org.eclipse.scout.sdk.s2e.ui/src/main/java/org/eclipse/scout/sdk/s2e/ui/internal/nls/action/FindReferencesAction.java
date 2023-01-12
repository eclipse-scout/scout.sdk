/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.action;

import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;
import org.eclipse.scout.sdk.s2e.ui.internal.nls.search.NlsFindKeyQuery;
import org.eclipse.search.ui.NewSearchUI;

/**
 * <h4>FindReferencesAction</h4>
 */
public class FindReferencesAction extends Action {

  private final TranslationManager m_project;
  private final String m_key;

  public FindReferencesAction(TranslationManager project, String key) {
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
