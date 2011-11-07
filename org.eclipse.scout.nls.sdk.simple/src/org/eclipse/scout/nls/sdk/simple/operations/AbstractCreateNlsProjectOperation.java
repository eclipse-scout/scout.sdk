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
package org.eclipse.scout.nls.sdk.simple.operations;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

public abstract class AbstractCreateNlsProjectOperation extends Job {

  protected static final String NL = System.getProperty("line.separator");
  protected static final String NLS_RUNNTIME_PLUGIN = "org.eclipse.scout.commons";

  private final NewNlsFileOperationDesc m_desc;

  protected AbstractCreateNlsProjectOperation(NewNlsFileOperationDesc desc) {
    super("Create new NLS Project...");
    m_desc = desc;
  }

  public final IStatus runSync() {
    return run(new NullProgressMonitor());
  }

  protected NewNlsFileOperationDesc getDesc() {
    return m_desc;
  }

  protected void createJavaClass(IProject plugin, IPath srcContainer, String packageName, String javaClassName, byte[] classContent, IProgressMonitor monitor) throws CoreException {
    IJavaProject jp = JavaCore.create(plugin);
    IPackageFragmentRoot root = jp.findPackageFragmentRoot(srcContainer);
    IPackageFragment fragment = root.createPackageFragment(packageName, true, monitor);
    String className = javaClassName;
    if (!className.endsWith(".java")) {
      className = className + ".java";
    }
    ICompilationUnit unit = fragment.createCompilationUnit(className, "", true, monitor);
    Assert.isTrue(unit.getUnderlyingResource() instanceof IFile);
    IFile classFile = (IFile) unit.getUnderlyingResource();
    classFile.setContents(new ByteArrayInputStream(classContent), true, false, monitor);
  }
}
