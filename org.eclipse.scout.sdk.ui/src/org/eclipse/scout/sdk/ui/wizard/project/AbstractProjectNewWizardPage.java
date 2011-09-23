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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scout.sdk.ui.wizard.AbstractWorkspaceWizardPage;

/**
 * <h3>AbstractWizardPage</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 1.0.8 06.03.2010
 */
public abstract class AbstractProjectNewWizardPage extends AbstractWorkspaceWizardPage {

  public AbstractProjectNewWizardPage(String pageName, String title, ImageDescriptor titleImage) {
    super(pageName, title, titleImage);
  }

  public AbstractProjectNewWizardPage(String pageName) {
    this(pageName, null, (ImageDescriptor) null);

  }

  public boolean performFinish(IProgressMonitor monitor) {
    return true;
  }

}
