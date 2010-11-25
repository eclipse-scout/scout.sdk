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
package org.eclipse.scout.nls.sdk.model.workspace;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.nls.sdk.internal.model.workspace.NlsType;
import org.eclipse.scout.nls.sdk.internal.model.workspace.project.NlsProject;
import org.eclipse.scout.nls.sdk.model.workspace.translationFile.ITranslationFile;

/** <h4>INlsWorkspace</h4> */
public interface INlsWorkspace {

  /**
   * @param project
   * @param monitor
   * @return
   * @throws CoreException
   */
  NlsProject findNlsProject(IProject project, IProgressMonitor monitor) throws CoreException;

  /**
   * @param project
   * @param fileName
   * @param monitor
   * @return
   * @throws CoreException
   */
  NlsProject findNlsProject(IProject project, String fileName, IProgressMonitor monitor) throws CoreException;

  /**
   * @param nlsResource
   * @param monitor
   * @return
   * @throws CoreException
   */
  NlsProject findNlsProject(IFile nlsResource, IProgressMonitor monitor) throws CoreException;

  /**
   * @param type
   * @param monitor
   * @return
   * @throws CoreException
   */
  NlsProject findNlsProject(IType type, IProgressMonitor monitor) throws CoreException;

  /**
   * @param nlsType
   * @param monitor
   * @return
   * @throws CoreException
   */
  ITranslationFile[] loadTranslationFiles(NlsType nlsType, IProgressMonitor monitor) throws CoreException;

}
