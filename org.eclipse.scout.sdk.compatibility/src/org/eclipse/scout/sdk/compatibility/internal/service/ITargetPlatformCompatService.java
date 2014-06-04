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
package org.eclipse.scout.sdk.compatibility.internal.service;

import java.io.File;
import java.net.URI;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public interface ITargetPlatformCompatService {
  IStatus resolveTargetPlatform(IFile targetFile, boolean loadPlatform, IProgressMonitor monitor) throws CoreException;

  IStatus resolveTargetPlatform(Set<File> absolutePaths, String targetName, boolean loadPlatform, IProgressMonitor monitor) throws CoreException;

  void addInstallableUnitToTarget(IFile targetFile, final String unitId, final String version, final String repository, IProgressMonitor monitor) throws CoreException;

  void removeInstallableUnitsFromTarget(IFile targetFile, String[] unitIds) throws CoreException;

  void addDirectoryLocationToTarget(IFile targetFile, String[] dirs) throws CoreException;

  URI getCurrentTargetFile() throws CoreException;
}
