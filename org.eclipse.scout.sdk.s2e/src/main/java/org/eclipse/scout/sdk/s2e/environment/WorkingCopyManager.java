/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.s2e.environment;

import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.sdk.core.util.CoreUtils.callInContext;
import static org.eclipse.scout.sdk.core.util.Ensure.newFail;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
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

    var wcm = new WorkingCopyManager();
    var save = false;
    try {
      runWithWorkingCopyManager(r, wcm);
      save = true;
    }
    finally {
      wcm.unregisterAll(save, monitorSupplier.get());
    }
  }

  static void runWithWorkingCopyManager(Runnable r, IWorkingCopyManager wcm) {
    callInContext(CURRENT, wcm, () -> {
      r.run();
      return null;
    });
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
  public synchronized boolean checkpoint(IProgressMonitor monitor) {
    return unregisterAllImpl(true, monitor);
  }

  synchronized void unregisterAll(boolean save, IProgressMonitor monitor) {
    try {
      unregisterAllImpl(save, monitor);
    }
    finally {
      m_open = false; // mark this instance as done. No more reconcile or register or checkpoint is allowed now.
    }
  }

  private boolean unregisterAllImpl(boolean save, IProgressMonitor monitor) {
    ensureOpen();
    try {
      var tryToSave = save && (monitor == null || !monitor.isCanceled()); // only save if asked for save and not canceled yet.
      if (tryToSave) {
        Collection<IResource> resourcesToSave = m_workingCopies.stream()
            .map(IJavaElement::getResource)
            .collect(toList());
        if (!resourcesToSave.isEmpty()) {
          var result = S2eUtils.makeCommittable(resourcesToSave);
          if (!result.isOK()) {
            tryToSave = false;
            SdkLog.info("Unable to make all resources committable. Save will be skipped.", new CoreException(result));
          }
        }
      }

      for (var icu : m_workingCopies) {
        releaseCompilationUnit(icu, monitor, tryToSave);
      }
      return tryToSave;
    }
    finally {
      // remove and mark as closed even there was an error in the unregister
      m_workingCopies.clear();
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

  @Override
  public int size() {
    return m_workingCopies.size();
  }

  private void ensureOpen() {
    Ensure.isTrue(isOpen(), "{} has already been committed/discarded. No more changes are allowed.", getClass().getName());
  }

  boolean isOpen() {
    return m_open;
  }
}
