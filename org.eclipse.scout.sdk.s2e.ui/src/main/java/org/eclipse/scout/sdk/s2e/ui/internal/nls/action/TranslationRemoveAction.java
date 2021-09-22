/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.action;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.scout.sdk.core.s.nls.ITranslation;
import org.eclipse.scout.sdk.core.s.nls.manager.TranslationManager;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;

public class TranslationRemoveAction extends Action {

  private final TranslationManager m_nlsProject;
  private final List<ITranslation> m_entries;

  public TranslationRemoveAction(TranslationManager project, ITranslation entry) {
    this("Remove " + entry.key(), project, singletonList(entry));
  }

  public TranslationRemoveAction(String name, TranslationManager project, List<? extends ITranslation> entries) {
    super(name);
    m_nlsProject = project;
    m_entries = new ArrayList<>(entries);
    setEnabled(project.isEditable());
    setImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.TextRemove));
  }

  @Override
  public void run() {
    m_nlsProject.removeTranslations(m_entries.stream().map(ITranslation::key));
  }
}
