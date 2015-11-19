/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.s2e.internal;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.job.AbstractJob;
import org.eclipse.scout.sdk.s2e.log.ScoutStatus;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;
import org.eclipse.scout.sdk.s2e.workspace.IWorkingCopyManager;

/**
 * <h3>WorkingCopyManager</h3>
 */
public class WorkingCopyManager implements IWorkingCopyManager {

  private final Set<ICompilationUnit> m_workingCopies;

  public WorkingCopyManager() {
    m_workingCopies = new LinkedHashSet<>();
  }

  @Override
  public synchronized boolean register(ICompilationUnit icu, IProgressMonitor monitor) throws JavaModelException {
    if (!m_workingCopies.contains(icu)) {
      icu.becomeWorkingCopy(monitor);
      m_workingCopies.add(icu);
      return true;
    }
    return false;
  }

  @Override
  public synchronized void unregisterAll(final IProgressMonitor monitor, final boolean save) {
    boolean tryToSave = save && !monitor.isCanceled(); // only save if asked for save and not cancelled yet.
    if (tryToSave) {
      List<IResource> resourcesToSave = new ArrayList<>(m_workingCopies.size());
      for (ICompilationUnit icu : m_workingCopies) {
        IResource resource = icu.getResource();
        resourcesToSave.add(resource);
      }

      if (!resourcesToSave.isEmpty()) {
        IStatus result = JdtUtils.makeCommittable(resourcesToSave);
        if (!result.isOK()) {
          tryToSave = false;
          SdkLog.warning("Unable to make all resources committable. Save will be skipped.", new CoreException(result));
        }
      }
    }

    for (ICompilationUnit icu : m_workingCopies) {
      releaseCompilationUnit(icu, monitor, tryToSave);
    }
    m_workingCopies.clear();
  }

  private static void releaseCompilationUnit(ICompilationUnit icu, IProgressMonitor monitor, boolean tryToSave) {
    try {
      if (tryToSave) {
        icu.commitWorkingCopy(true, monitor);
        indexCompilationUnitSync(icu);
      }
    }
    catch (Exception e) {
      SdkLog.warning("Unable to commit working copy '" + icu.getElementName() + "'.", e);
    }
    finally {
      try {
        icu.discardWorkingCopy();
      }
      catch (JavaModelException e) {
        SdkLog.warning("Unable to discard working copy '" + icu.getElementName() + "'.", e);
      }
    }
  }

  private static void indexCompilationUnitSync(ICompilationUnit icu) {
    IJavaProject jp = icu.getJavaProject();
    IndexManager im = JavaModelManager.getIndexManager();
    IPath containerPath = jp.getProject().getFullPath();

    SourceElementParser parser = im.getSourceElementParser(jp, null/*requestor will be set by indexer*/);
    im.addSource((IFile) icu.getResource(), containerPath, parser);
    AbstractJob.waitForJobFamily(containerPath.toString());
  }

  @Override
  public synchronized void reconcile(ICompilationUnit icu, IProgressMonitor monitor) throws CoreException {
    if (!m_workingCopies.contains(icu)) {
      throw new CoreException(new ScoutStatus("compilation unit " + icu.getElementName() + " has not been registered"));
    }
    icu.reconcile(ICompilationUnit.NO_AST, true, icu.getOwner(), monitor);
  }
}
