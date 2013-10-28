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
package org.eclipse.scout.sdk.util.internal.typecache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.jdt.JdtUtility;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>WorkingCopyManager</h3> ...
 */
@SuppressWarnings("restriction")
public class WorkingCopyManager implements IWorkingCopyManager {

  private final Object LOCK = new Object();
  private List<ICompilationUnit> m_workingCopies = new ArrayList<ICompilationUnit>();

  public WorkingCopyManager() {
  }

  @Override
  public boolean register(ICompilationUnit icu, IProgressMonitor monitor) throws JavaModelException {
    SdkUtilActivator.logInfo(getClass().getSimpleName() + ": register workingcopy for " + icu.getElementName());
    if (icu.isReadOnly()) {
      throw new IllegalArgumentException("try to get a working copy of the read only icu '" + icu.getElementName() + "'.");
    }
    synchronized (LOCK) {
      if (!m_workingCopies.contains(icu)) {
        icu.becomeWorkingCopy(monitor);
        m_workingCopies.add(icu);
        return true;
      }
    }
    return false;
  }

  @Override
  public void unregisterAll(IProgressMonitor monitor) {
    SdkUtilActivator.logInfo(getClass().getSimpleName() + ": unregister all workingcopies");
    synchronized (LOCK) {
      for (Iterator<ICompilationUnit> it = m_workingCopies.iterator(); it.hasNext();) {
        releaseCompilationUnit(it.next(), monitor);
      }
      m_workingCopies.clear();
    }
  }

  @Override
  public void unregister(ICompilationUnit icu, IProgressMonitor monitor) {
    SdkUtilActivator.logInfo(getClass().getSimpleName() + ": unregister workingcopy for: '" + icu.getElementName() + "'");
    synchronized (LOCK) {
      if (m_workingCopies.remove(icu)) {
        releaseCompilationUnit(icu, monitor);
      }
    }
  }

  public void discardAll(IProgressMonitor monitor) {

  }

  private void releaseCompilationUnit(ICompilationUnit icu, IProgressMonitor monitor) {

    try {
      if (!monitor.isCanceled()) {
        icu.commitWorkingCopy(true, monitor);
        indexCompilationUnitSync(icu);
      }
    }
    catch (JavaModelException e) {
      SdkUtilActivator.logError("could not commit working copy '" + icu.getElementName() + "'", e);
    }
    finally {
      try {
        icu.discardWorkingCopy();
      }
      catch (JavaModelException e) {
        SdkUtilActivator.logError("could not discard working copy '" + icu.getElementName() + "'", e);
      }
    }
  }

  private void indexCompilationUnitSync(ICompilationUnit icu) {
    IJavaProject jp = icu.getJavaProject();
    IndexManager im = JavaModelManager.getIndexManager();
    IPath containerPath = jp.getProject().getFullPath();

    SourceElementParser parser = im.getSourceElementParser(jp, null/*requestor will be set by indexer*/);
    im.addSource((IFile) icu.getResource(), containerPath, parser);
    JdtUtility.waitForJobFamily(containerPath.toString());
  }

  @Override
  public void reconcile(ICompilationUnit icu, IProgressMonitor monitor) throws CoreException {
    SdkUtilActivator.logInfo(getClass().getSimpleName() + ": reconcile compilation unit '" + icu.getElementName() + "'");
    synchronized (LOCK) {
      if (!m_workingCopies.contains(icu)) {
        throw new CoreException(new ScoutStatus("compilation unit " + icu.getElementName() + " has not been registered"));
      }
      icu.reconcile(ICompilationUnit.NO_AST, true, icu.getOwner(), monitor);
    }
  }
}
