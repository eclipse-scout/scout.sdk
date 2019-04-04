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
package org.eclipse.scout.sdk.s2e.environment;

import static org.eclipse.scout.sdk.core.util.CoreUtils.runInContext;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

/**
 * <h3>WorkingCopyManager</h3>
 */
public final class WorkingCopyManager implements IWorkingCopyManager {

  private static final ThreadLocal<IWorkingCopyManager> CURRENT = ThreadLocal.withInitial(() -> null);
  private final Set<ICompilationUnit> m_workingCopies;
  private volatile boolean m_open;

  /**
   * @return the {@link IWorkingCopyManager} that is associated with the current {@link Thread}. Never returns
   *         {@code null}.
   *         <p>
   *         Use {@link EclipseEnvironment#callInEclipseEnvironment(BiFunction)} to schedule an operation in which an
   *         {@link IWorkingCopyManager} is available.
   * @throws IllegalArgumentException
   *           if no {@link IWorkingCopyManager} is available for the current {@link Thread}.
   */
  public static IWorkingCopyManager currentWorkingCopyManager() {
    return Ensure.notNull(CURRENT.get(), "No working-copy-manager available in the current context.");
  }

  /**
   * Executes the specified {@link Runnable} within a new {@link IWorkingCopyManager}. From within the {@link Runnable}
   * the method {@link #currentWorkingCopyManager()} returns a valid {@link IWorkingCopyManager}.
   *
   * @param r
   *          The {@link Runnable} to execute. Must not be {@code null}.
   * @param monitorSupplier
   *          A {@link Supplier} for an {@link IProgressMonitor} that will be used in case the working copies are saved.
   *          May not be {@code null}.
   */
  public static void runWithWorkingCopyManager(Runnable r, Supplier<IProgressMonitor> monitorSupplier) {
    Ensure.notNull(r);
    Ensure.notNull(monitorSupplier);

    IWorkingCopyManager wcm = new WorkingCopyManager();
    boolean save = false;
    try {
      runWithWorkingCopyManager(r, wcm);
      save = true;
    }
    finally {
      wcm.unregisterAll(save, monitorSupplier.get());
    }
  }

  static void runWithWorkingCopyManager(Runnable r, IWorkingCopyManager wcm) {
    runInContext(CURRENT, wcm, r);
  }

  private WorkingCopyManager() {
    m_workingCopies = new LinkedHashSet<>();
    m_open = true;
  }

  @Override
  public synchronized boolean register(ICompilationUnit icu, IProgressMonitor monitor) throws JavaModelException {
    ensureOpen();
    if (!m_workingCopies.contains(icu)) {
      icu.becomeWorkingCopy(monitor);
      m_workingCopies.add(icu);
      return true;
    }
    return false;
  }

  @Override
  public synchronized void unregisterAll(boolean save, IProgressMonitor monitor) {
    ensureOpen();
    try {
      boolean tryToSave = save && (monitor == null || !monitor.isCanceled()); // only save if asked for save and not canceled yet.
      if (tryToSave) {
        Collection<IResource> resourcesToSave = new ArrayList<>(m_workingCopies.size());
        for (ICompilationUnit icu : m_workingCopies) {
          IResource resource = icu.getResource();
          resourcesToSave.add(resource);
        }

        if (!resourcesToSave.isEmpty()) {
          IStatus result = S2eUtils.makeCommittable(resourcesToSave);
          if (!result.isOK()) {
            tryToSave = false;
            SdkLog.warning("Unable to make all resources committable. Save will be skipped.", new CoreException(result));
          }
        }
      }

      for (ICompilationUnit icu : m_workingCopies) {
        releaseCompilationUnit(icu, monitor, tryToSave);
      }
    }
    finally {
      // remove and mark as closed even there was an error in the unregister
      m_workingCopies.clear();
      m_open = false; // mark this instance as done. No more reconcile or register is allowed now.
    }
  }

  private static void releaseCompilationUnit(ICompilationUnit icu, IProgressMonitor monitor, boolean tryToSave) {
    try {
      if (tryToSave) {
        icu.commitWorkingCopy(true, monitor);
      }
    }
    catch (RuntimeException | JavaModelException e) {
      SdkLog.warning("Unable to commit working copy '{}'.", icu.getElementName(), e);
    }
    finally {
      try {
        icu.discardWorkingCopy();
      }
      catch (JavaModelException e) {
        SdkLog.warning("Unable to discard working copy '{}'.", icu.getElementName(), e);
      }
    }
  }

  @Override
  public synchronized void reconcile(ICompilationUnit icu, IProgressMonitor monitor) throws JavaModelException {
    ensureOpen();
    if (!m_workingCopies.contains(icu)) {
      throw newFail("compilation unit {} has not been registered", icu.getElementName());
    }
    icu.reconcile(ICompilationUnit.NO_AST, true, icu.getOwner(), monitor);
  }

  private void ensureOpen() {
    Ensure.isTrue(isOpen(), "{} has already been commited/rollbacked. No more changes are allowed.", getClass().getName());
  }

  boolean isOpen() {
    return m_open;
  }
}
