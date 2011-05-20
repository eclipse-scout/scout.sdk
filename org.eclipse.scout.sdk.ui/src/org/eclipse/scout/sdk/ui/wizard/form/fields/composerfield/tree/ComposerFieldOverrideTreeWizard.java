/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.ui.wizard.form.fields.composerfield.tree;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;

/**
 * <h3>{@link ComposerFieldOverrideTreeWizard}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 01.09.2010
 */
public class ComposerFieldOverrideTreeWizard extends AbstractWorkspaceWizard {

  private final IType m_composerField;

  public ComposerFieldOverrideTreeWizard(IType composerField) {
    setWindowTitle("Override Composer Field Tree");
    m_composerField = composerField;
  }

  @Override
  public void addPages() {
    addPage(new ComposerFieldOverrideTreeWizardPage(getComposerField()));
  }

  /**
   * @return the composerField
   */
  public IType getComposerField() {
    return m_composerField;
  }
}
