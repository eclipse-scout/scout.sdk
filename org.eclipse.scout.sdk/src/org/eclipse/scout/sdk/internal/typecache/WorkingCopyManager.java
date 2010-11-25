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
package org.eclipse.scout.sdk.internal.typecache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.scout.sdk.ScoutSdk;
import org.eclipse.scout.sdk.ScoutStatus;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

/**
 * <h3>BCWorkingCopyLevel</h3> ...
 */
public class WorkingCopyManager implements IScoutWorkingCopyManager {

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

  public void register(ICompilationUnit icu, IProgressMonitor monitor) throws JavaModelException {
    register(icu, true, monitor);
  }

  public void register(ICompilationUnit icu, boolean wellform, IProgressMonitor monitor) throws JavaModelException {
    synchronized (LOCK) {
      boolean added = m_workingCopies.add(icu);
      if (added) {
        ScoutSdk.logInfo("#REGISTER working copy " + icu.getElementName());
        icu.becomeWorkingCopy(monitor);
      }
      if (wellform) {
        m_wellformWorkingCopies.add(icu);
      }
    }
  }

  public void unregister(ICompilationUnit icu, IProgressMonitor monitor) {
    try {
      synchronized (LOCK) {
        if (!m_workingCopies.contains(icu)) {
          return;
        }
      }
      //
      boolean wellform = false;
      synchronized (LOCK) {
        wellform = m_wellformWorkingCopies.remove(icu);
      }
      if (!icu.exists()) {
        return;
      }
      if (!monitor.isCanceled() && wellform) {
        // wellform asynchronous in workspace rule
        // ProcessJob job = new ProcessJob(new WellformCompilationUnitOperation(icu));
        // job.schedule();

      }
      synchronized (LOCK) {
        m_workingCopies.remove(icu);
      }
      try {
        if (!monitor.isCanceled()) {
          icu.commitWorkingCopy(true, monitor);
        }
        else {
          icu.discardWorkingCopy();
          icu.becomeWorkingCopy(monitor);
          icu.commitWorkingCopy(true, monitor);
        }
      }
      catch (JavaModelException e) {
        ScoutSdk.logError("could not commit working copy '" + icu.getElementName() + "'", e);
      }
      finally {
        try {
          icu.discardWorkingCopy();
        }
        catch (JavaModelException e) {
          ScoutSdk.logError("could not discard working copy '" + icu.getElementName() + "'", e);
        }
      }
    }
    finally {
    }
  }

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
