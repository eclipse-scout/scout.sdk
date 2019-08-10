/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.ui.internal.nls.editor.importexport;

import java.util.List;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.scout.sdk.core.s.nls.TranslationStoreStack;
import org.eclipse.scout.sdk.s2e.ui.ISdkIcons;
import org.eclipse.scout.sdk.s2e.ui.internal.S2ESdkUiActivator;

/**
 * <h4>NlsExportWizard</h4>
 *
 * @since 1.1.0 (11.11.2010)
 */
public class TranslationImportExportWizard extends Wizard {

  private final TranslationStoreStack m_project;
  private final String m_title;
  private final String m_description;
  private final List<TranslationImportExportWizardExtension> m_extensions;

  public TranslationImportExportWizard(String title, String description, TranslationStoreStack project, List<TranslationImportExportWizardExtension> extensions) {
    m_title = title;
    m_description = description;
    m_project = project;
    m_extensions = extensions;
    setWindowTitle(title);
    setHelpAvailable(false);
    setDefaultPageImageDescriptor(S2ESdkUiActivator.getImageDescriptor(ISdkIcons.ScoutProjectNewWizBanner));
  }

  @Override
  public void addPages() {
    addPage(new TranslationImportExportWizardPage(m_title, m_description, m_project, m_extensions));
  }

  @Override
  public boolean performFinish() {
    return false;
  }

  @Override
  public boolean needsPreviousAndNextButtons() {
    return true;
  }

  @Override
  public boolean canFinish() {
    return false;
  }
}
