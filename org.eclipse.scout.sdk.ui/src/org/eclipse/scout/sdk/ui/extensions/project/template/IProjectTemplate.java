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
package org.eclipse.scout.sdk.ui.extensions.project.template;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizard;

/**
 * Represents a scout project template
 */
public interface IProjectTemplate {

  /**
   * @return The name of the template shown in the selection list in the wizard page
   */
  String getText();

  /**
   * @return The description of the template shown when the template is selected.
   */
  String getDescription();

  /**
   * @return The id of the template
   */
  String getId();

  /**
   * @param wizard
   *          The context wizard.
   * @return true if the template should be available to the user. False otherwise.
   */
  boolean isApplicable(IScoutProjectWizard wizard);

  /**
   * @return The status of the template.
   */
  IStatus getStatus();
}
