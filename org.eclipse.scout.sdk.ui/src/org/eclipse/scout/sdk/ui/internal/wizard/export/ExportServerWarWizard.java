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
package org.eclipse.scout.sdk.ui.internal.wizard.export;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class ExportServerWarWizard extends AbstractWorkspaceWizard {

  private ExportServerWarWizardPage m_page1;
  private IType m_declaringType;

  public ExportServerWarWizard(IScoutBundle serverBundle) {
    setWindowTitle("Export to WAR");
    m_page1 = new ExportServerWarWizardPage(serverBundle.getScoutProject());
    addPage(m_page1);
  }

}
