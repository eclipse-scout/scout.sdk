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
package org.eclipse.scout.sdk.ui.wizard.outline;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.Texts;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizard;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

public class OutlineNewWizard extends AbstractWorkspaceWizard {

  private OutlineNewWizardPage m_page1;

  public OutlineNewWizard(IScoutBundle clientBundle) {
    this(clientBundle, null);
  }

  public OutlineNewWizard(IScoutBundle clientBunldle, IType desktopType) {
    setWindowTitle(Texts.get("NewOutline"));
    m_page1 = new OutlineNewWizardPage(clientBunldle, desktopType);

    addPage(m_page1);
  }
}
