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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.IScoutProject;

/**
 * <h3>{@link ProjectWithFormTemplate}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 10.02.2011
 */
public class ProjectWithFormTemplate implements IScoutProjectTemplate {

  @Override
  public void applyTemplate(IScoutProject scoutProject, IProgressMonitor monitor, IScoutWorkingCopyManager manager) {

  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public String getTemplateName() {
    return "Application with a single form.";
  }

  @Override
  public IWizardPage[] getWizardPages() {
    return null;
  }

}
