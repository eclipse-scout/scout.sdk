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
package org.eclipse.scout.sdk.ui.wizard.project;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.scout.sdk.ui.wizard.AbstractScoutWizardPage;
import org.eclipse.scout.sdk.workspace.IScoutBundle;

/**
 *
 */
public interface IScoutProjectWizard {

  IScoutProjectWizardPage getProjectWizardPage();

  /**
   * @param name
   * @return
   */
  AbstractScoutWizardPage getPage(String name);

  /**
   * @param page
   */
  void addPage(IWizardPage page);

  /**
   * @return
   */
  IScoutBundle getScoutProject();

}
