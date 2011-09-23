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
package org.eclipse.scout.sdk.ui.extensions.bundle;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.scout.sdk.operation.template.ITemplateVariableSet;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;
import org.eclipse.scout.sdk.ui.wizard.project.IScoutProjectWizard;

/**
 *
 */
public interface IScoutBundleProvider {

  IJavaProject createBundle(ITemplateVariableSet variables, IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager);

  void bundleSelectionChanged(IScoutProjectWizard wizard, boolean selected);

  IStatus getStatus(IScoutProjectWizard wizard);

}
