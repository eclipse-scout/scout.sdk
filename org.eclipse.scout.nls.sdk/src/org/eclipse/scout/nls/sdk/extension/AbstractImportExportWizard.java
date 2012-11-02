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
package org.eclipse.scout.nls.sdk.extension;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;

/**
 * <h4>AbstractImportExportWizard</h4>
 * 
 * @author Andreas Hoegger
 * @since 1.1.0 (12.11.2010)
 */
public abstract class AbstractImportExportWizard extends Wizard {

  private INlsProject m_nlsProject;

  public void setNlsProject(INlsProject nlsProject) {
    m_nlsProject = nlsProject;
  }

  public INlsProject getNlsProject() {
    return m_nlsProject;
  }

}
