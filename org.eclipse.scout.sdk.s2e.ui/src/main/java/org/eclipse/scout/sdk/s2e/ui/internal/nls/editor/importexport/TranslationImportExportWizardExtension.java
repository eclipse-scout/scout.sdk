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
package org.eclipse.scout.sdk.s2e.ui.internal.nls.editor.importexport;

import org.eclipse.scout.sdk.core.util.SdkException;
import org.eclipse.scout.sdk.s2e.ui.wizard.AbstractImportExportWizard;

/**
 * <h4>ImportExportExtensionPoint</h4>
 *
 * @since 1.1.0 (11.11.2010)
 */
public final class TranslationImportExportWizardExtension {

  private final Class<? extends AbstractImportExportWizard> m_wizard;
  private final String m_name;

  TranslationImportExportWizardExtension(Class<? extends AbstractImportExportWizard> wizard, String name) {
    m_wizard = wizard;
    m_name = name;
  }

  public Class<? extends AbstractImportExportWizard> getWizardClass() {
    return m_wizard;
  }

  public AbstractImportExportWizard createWizard() {
    try {
      return m_wizard.getConstructor().newInstance();
    }
    catch (ReflectiveOperationException e) {
      throw new SdkException(e);
    }
  }

  public String getName() {
    return m_name;
  }
}
