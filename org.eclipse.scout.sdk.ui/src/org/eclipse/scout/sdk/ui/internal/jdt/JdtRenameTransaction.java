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
package org.eclipse.scout.sdk.ui.internal.jdt;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.ui.refactoring.RenameSupport;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.scout.sdk.ScoutStatus;
import org.eclipse.scout.sdk.ui.ScoutSdkUi;
import org.eclipse.swt.widgets.Shell;

/**
 * <h3>JdtRenameTransaction</h3> ...
 */
public class JdtRenameTransaction {
  private ArrayList<RenameSupport> m_list = new ArrayList<RenameSupport>();
  private HashSet<ICompilationUnit> m_touchedCompilationUntis = new HashSet<ICompilationUnit>();

  public JdtRenameTransaction() {
  }

  public static int getDefaultFlags() {
    return RenameSupport.UPDATE_REFERENCES | RenameSupport.UPDATE_TEXTUAL_MATCHES;
  }

  public void add(ICompilationUnit e, String newName) throws CoreException {
    addRenameSupport(RenameSupport.create(e, newName, getDefaultFlags()));
    m_touchedCompilationUntis.add(e);
  }

  public void add(IField e, String newName) throws CoreException {
    addRenameSupport(RenameSupport.create(e, newName, getDefaultFlags()));
    m_touchedCompilationUntis.add(e.getCompilationUnit());
  }

  public void add(IJavaProject e, String newName) throws CoreException {
    addRenameSupport(RenameSupport.create(e, newName, getDefaultFlags()));
  }

  public void add(ILocalVariable e, String newName) throws CoreException {
    addRenameSupport(RenameSupport.create(e, newName, getDefaultFlags()));

  }

  public void add(IMethod e, String newName) throws CoreException {
    addRenameSupport(RenameSupport.create(e, newName, getDefaultFlags()));
    m_touchedCompilationUntis.add(e.getCompilationUnit());
  }

  public void add(IPackageFragment e, String newName) throws CoreException {
    addRenameSupport(RenameSupport.create(e, newName, getDefaultFlags()));

  }

  public void add(IPackageFragmentRoot e, String newName) throws CoreException {
    addRenameSupport(RenameSupport.create(e, newName));
  }

  public void add(IType e, String newName) throws CoreException {
    addRenameSupport(RenameSupport.create(e, newName, getDefaultFlags()));
    m_touchedCompilationUntis.add(e.getCompilationUnit());
  }

  public void add(ITypeParameter e, String newName) throws CoreException {
    addRenameSupport(RenameSupport.create(e, newName, getDefaultFlags()));
    m_touchedCompilationUntis.add(e.getDeclaringMember().getCompilationUnit());
  }

  public void addRenameSupport(RenameSupport r) {
    m_list.add(r);
  }

  public void commit(Shell shell) throws CoreException {
    // precheck
    for (RenameSupport r : m_list) {
      if (!r.preCheck().isOK()) {
        throw new CoreException(new ScoutStatus("preCheck failed"));

      }
    }
    // do it
    try {
      // Shell shell=SDEUI.getShell();
      for (RenameSupport r : m_list) {
        r.perform(shell, new ProgressMonitorDialog(shell));
      }
      for (ICompilationUnit icu : m_touchedCompilationUntis) {
        icu.reconcile(
            ICompilationUnit.NO_AST,
            false /* don't force problem detection */,
            null /* use primary owner */,
            null /* no progress monitor */);
      }
      m_touchedCompilationUntis.clear();
    }
    catch (Throwable t) {
      ScoutSdkUi.logError("rename failed.", t);
    }

  }
}
