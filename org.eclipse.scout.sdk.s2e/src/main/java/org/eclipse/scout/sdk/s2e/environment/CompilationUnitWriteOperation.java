/*
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.s2e.environment;

import static org.eclipse.scout.sdk.s2e.environment.WorkingCopyManager.currentWorkingCopyManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.core.log.SdkLog;
import org.eclipse.scout.sdk.core.util.Ensure;
import org.eclipse.scout.sdk.core.util.JavaTypes;
import org.eclipse.scout.sdk.core.util.Strings;
import org.eclipse.scout.sdk.s2e.operation.OrganizeImportOperation;
import org.eclipse.scout.sdk.s2e.operation.SourceFormatOperation;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

/**
 * <h3>{@link CompilationUnitWriteOperation}</h3>
 * <p>
 * Change the content of a new or existing compilation unit
 *
 * @since 5.1.0
 */
public class CompilationUnitWriteOperation implements IResourceWriteOperation {
  private final IJavaProject m_project;
  private final IPackageFragmentRoot m_root;
  private final String m_packageName;
  private final String m_fileName;
  private final CharSequence m_content;

  private ICompilationUnit m_createdCompilationUnit;

  public CompilationUnitWriteOperation(ICompilationUnit existingUnit, CharSequence content) {
    this(JdtUtils.getSourceFolder(existingUnit), JdtUtils.getPackage(existingUnit), existingUnit.getElementName(), content);
  }

  public CompilationUnitWriteOperation(IPackageFragmentRoot srcFolder, String packageName, String fileName, CharSequence content) {
    m_root = Ensure.notNull(srcFolder);
    m_project = srcFolder.getJavaProject();
    if (Strings.isBlank(packageName)) {
      m_packageName = ""; // default package
    }
    else {
      m_packageName = packageName;
    }
    m_fileName = Ensure.notNull(fileName);
    m_content = Ensure.notNull(content);
  }

  public ICompilationUnit getCreatedCompilationUnit() {
    return m_createdCompilationUnit;
  }

  @Override
  public IResource getAffectedResource() {
    IResource result = getSourceFolder().getResource();
    IPackageFragment packageFragment = getSourceFolder().getPackageFragment(getPackageName());
    if (JdtUtils.exists(packageFragment)) {
      result = packageFragment.getResource();
      ICompilationUnit compilationUnit = packageFragment.getCompilationUnit(getFileName());
      if (compilationUnit.exists()) {
        result = compilationUnit.getResource();
      }
    }
    return result;
  }

  @Override
  public void accept(EclipseProgress progress) {
    progress.init(toString(), 5);

    try {
      String newSource = getSourceFormatted(getContent().toString(), m_project, progress.newChild(1));
      IPackageFragment pck = getSourceFolder().getPackageFragment(getPackageName());
      if (!JdtUtils.exists(pck)) {
        pck = getSourceFolder().createPackageFragment(getPackageName(), true, progress.newChild(1).monitor());
      }

      m_createdCompilationUnit = pck.getCompilationUnit(getFileName());
      progress.setWorkRemaining(2);
      if (!m_createdCompilationUnit.exists()) {
        m_createdCompilationUnit = pck.createCompilationUnit(getFileName(), newSource, true, progress.newChild(1).monitor());
        currentWorkingCopyManager().register(m_createdCompilationUnit, progress.newChild(1).monitor());
        organizeImports(m_createdCompilationUnit, progress.newChild(1));
      }
      else {
        // only write if changed
        if (!ResourceWriteOperation.areContentsEqual((IFile) m_createdCompilationUnit.getResource(), newSource)) {
          currentWorkingCopyManager().register(m_createdCompilationUnit, progress.newChild(1).monitor());
          m_createdCompilationUnit.getBuffer().setContents(newSource);
          organizeImports(m_createdCompilationUnit, progress.newChild(1));
        }
        progress.setWorkRemaining(0);
      }
    }
    catch (JavaModelException e) {
      SdkLog.error("Could not {}", this, e);
    }
  }

  protected String getSourceFormatted(String unformattedJavaSource, IJavaProject settings, EclipseProgress progress) {
    try {
      SourceFormatOperation op = new SourceFormatOperation(settings, new Document(unformattedJavaSource));
      op.accept(progress.newChild(10));
      return op.getDocument().get();
    }
    catch (RuntimeException e) {
      // if source format fails: still write compilation unit
      SdkLog.warning("Unable to format source of compilation unit '{}'.", getFileName(), e);
      return unformattedJavaSource;
    }
  }

  protected void organizeImports(ICompilationUnit icu, EclipseProgress progress) {
    try {
      new OrganizeImportOperation(icu).accept(progress.newChild(10));
    }
    catch (RuntimeException e) {
      // if organize import fails: still write compilation unit
      SdkLog.warning("Unable to organize imports of compilation unit '{}'.", getFileName(), e);
    }
  }

  public IPackageFragmentRoot getSourceFolder() {
    return m_root;
  }

  public String getPackageName() {
    return m_packageName;
  }

  public String getFileName() {
    return m_fileName;
  }

  public CharSequence getContent() {
    return m_content;
  }

  @Override
  public IFile getFile() {
    if (!JdtUtils.exists(m_createdCompilationUnit)) {
      return null;
    }
    IResource resource = m_createdCompilationUnit.getResource();
    if (resource == null || !resource.exists() || resource.getType() != IResource.FILE) {
      return null;
    }
    return (IFile) resource;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Write ");
    sb.append(getSourceFolder().getPath()).append('/');
    if (!getPackageName().isEmpty()) {
      sb.append(getPackageName().replace(JavaTypes.C_DOT, '/')).append('/');
    }
    sb.append(getFileName());
    return sb.toString();
  }
}
