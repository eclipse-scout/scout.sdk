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
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.scout.sdk.util.internal.SdkUtilActivator;
import org.eclipse.scout.sdk.util.log.ScoutStatus;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>WorkingCopyManager</h3> ...
 */
@SuppressWarnings("restriction")
public class WorkingCopyManager implements IWorkingCopyManager {

  private Object LOCK = new Object();
  private HashSet<ICompilationUnit> m_workingCopies = new HashSet<ICompilationUnit>();
  private HashSet<ICompilationUnit> m_wellformWorkingCopies = new HashSet<ICompilationUnit>();

  public WorkingCopyManager() {
  }

  private List<ICompilationUnit> getWorkingCopies() {
    synchronized (LOCK) {
      return new ArrayList<ICompilationUnit>(m_workingCopies);
    }
  }

  @Override
  public void register(ICompilationUnit icu, IProgressMonitor monitor) throws JavaModelException {
    register(icu, true, monitor);
  }

  @Override
  public void register(ICompilationUnit icu, boolean wellform, IProgressMonitor monitor) throws JavaModelException {
    synchronized (LOCK) {
      boolean added = m_workingCopies.add(icu);
      if (added) {
        SdkUtilActivator.logInfo("#REGISTER working copy " + icu.getElementName());
        icu.becomeWorkingCopy(monitor);
      }
      if (wellform) {
        m_wellformWorkingCopies.add(icu);
      }
    }
  }

  @Override
  public void unregister(ICompilationUnit icu, IProgressMonitor monitor) {
    try {
      synchronized (LOCK) {
        if (!m_workingCopies.contains(icu)) {
          return;
        }
      }
      //
      synchronized (LOCK) {
        /*wellform = */m_wellformWorkingCopies.remove(icu);
      }
      if (!icu.exists()) {
        return;
      }
      synchronized (LOCK) {
        m_workingCopies.remove(icu);
      }
      try {
        if (!monitor.isCanceled()) {
          icu.commitWorkingCopy(true, monitor);
          indexCompilationUnitSync(icu);
        }
        else {
          icu.discardWorkingCopy();
          icu.becomeWorkingCopy(monitor);
          icu.commitWorkingCopy(true, monitor);
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
    finally {
    }
  }

  private void indexCompilationUnitSync(ICompilationUnit icu) {
    IJavaProject jp = icu.getJavaProject();
    IndexManager im = JavaModelManager.getIndexManager();
    IPath containerPath = jp.getProject().getFullPath();

    SourceElementParser parser = im.getSourceElementParser(jp, null/*requestor will be set by indexer*/);
    im.addSource((IFile) icu.getResource(), containerPath, parser);
    waitForJob(containerPath.toString());
  }

  private void waitForJob(String family) {
    try {
      boolean wasInterrupted = false;
      do {
        try {
          Job.getJobManager().join(family, null);
          wasInterrupted = false;
        }
        catch (InterruptedException e) {
          wasInterrupted = true;
        }
      }
      while (wasInterrupted);
    }
    catch (Throwable t) {
    }
  }

  @Override
  public void unregisterAll(IProgressMonitor monitor) {
    for (ICompilationUnit icu : getWorkingCopies()) {
      unregister(icu, monitor);
    }
    m_workingCopies.clear();
    m_wellformWorkingCopies.clear();
  }

  private void checkRegistered(ICompilationUnit icu) throws CoreException {
    synchronized (LOCK) {
      if (!m_workingCopies.contains(icu)) {
        throw new CoreException(new ScoutStatus("compilation unit " + icu.getElementName() + " has not been registered"));
      }
    }
  }

  @Override
  public CompilationUnit reconcile(ICompilationUnit icu, IProgressMonitor monitor) throws CoreException {
    CompilationUnit ast = null;
    try {
      checkRegistered(icu);
      ast = icu.reconcile(AST.JLS3, true, icu.getOwner(), monitor);
    }
    finally {
    }
    return ast;
  }

}
