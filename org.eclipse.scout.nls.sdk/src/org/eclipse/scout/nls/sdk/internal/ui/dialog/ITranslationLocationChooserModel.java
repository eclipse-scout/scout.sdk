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
package org.eclipse.scout.nls.sdk.internal.ui.dialog;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.scout.nls.sdk.internal.jdt.INlsFolder;
import org.eclipse.scout.nls.sdk.model.workspace.project.INlsProject;

public interface ITranslationLocationChooserModel {
  IProject getProject();

  INlsProject getNlsProject();

  IPath getPath();

  INlsFolder getFolder();

}
