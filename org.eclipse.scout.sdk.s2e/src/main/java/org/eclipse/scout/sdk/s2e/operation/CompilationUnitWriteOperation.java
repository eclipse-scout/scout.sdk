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
package org.eclipse.scout.sdk.s2e.operation;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.core.model.api.IJavaEnvironment;
import org.eclipse.scout.sdk.core.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.util.S2eUtils;

/**
 * <h3>{@link CompilationUnitWriteOperation}</h3>
 * <p>
 * Change the content of a new or existing compilation unit
 *
 * @author Ivan Motsch, Matthias Villiger
 * @since 5.1.0
 */
public class CompilationUnitWriteOperation implements IFileWriteOperation {
  private final IJavaProject m_project;
  private final IPackageFragmentRoot m_root;
  private final String m_packageName;
  private final String m_fileName;
  private final String m_content;

  private ICompilationUnit m_createdCompilationUnit;

  public CompilationUnitWriteOperation(IType existingJdtType, String content) {
    this(Validate.notNull(existingJdtType).getCompilationUnit(), content);
  }

  public CompilationUnitWriteOperation(ICompilationUnit existingUnit, String content) {
    this((IPackageFragment) Validate.notNull(existingUnit).getAncestor(IJavaElement.PACKAGE_FRAGMENT), existingUnit.getElementName(), content);
  }

  public CompilationUnitWriteOperation(IPackageFragment pck, String fileName, String content) {
    this((IPackageFragmentRoot) Validate.notNull(pck).getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT), pck.getElementName(), fileName, content);
  }

  public CompilationUnitWriteOperation(IPackageFragmentRoot srcFolder, ICompilationUnitSourceBuilder contentBuilder) {
    this(srcFolder, contentBuilder, null);
  }

  public CompilationUnitWriteOperation(IPackageFragmentRoot srcFolder, ICompilationUnitSourceBuilder contentBuilder, IJavaEnvironment env) {
    this(srcFolder, contentBuilder.getPackageName(), contentBuilder.getElementName(), S2eUtils.createJavaCode(contentBuilder, srcFolder.getJavaProject(), env));
  }

  public CompilationUnitWriteOperation(IPackageFragmentRoot srcFolder, String packageName, String fileName, String content) {
    m_root = Validate.notNull(srcFolder);
    m_project = srcFolder.getJavaProject();
    if (StringUtils.isBlank(packageName)) {
      m_packageName = ""; // default package
    }
    else {
      m_packageName = packageName;
    }
    m_fileName = Validate.notNull(fileName);
    m_content = content;
  }

  @Override
  public String getOperationName() {
    StringBuilder sb = new StringBuilder("write ");
    sb.append(getSourceFolder().getPath().toString()).append('/');
    if (!getPackageName().isEmpty()) {
      sb.append(getPackageName().replace('.', '/')).append('/');
    }
    sb.append(getFileName());
    return sb.toString();
  }

  public ICompilationUnit getCreatedCompilationUnit() {
    return m_createdCompilationUnit;
  }

  @Override
  public void validate() {
    // already done in constructor
  }

  @Override
  public IResource getAffectedResource() {
    IResource result = getSourceFolder().getResource();
    IPackageFragment packageFragment = getSourceFolder().getPackageFragment(getPackageName());
    if (packageFragment.exists()) {
      result = packageFragment.getResource();
      ICompilationUnit compilationUnit = packageFragment.getCompilationUnit(getFileName());
      if (compilationUnit.exists()) {
        result = compilationUnit.getResource();
      }
    }
    return result;
  }

  protected static String getSourceFormatted(String unformattedJavaSource, IJavaProject settings, IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    SourceFormatOperation op = new SourceFormatOperation(settings, new Document(unformattedJavaSource));
    op.validate();
    op.run(monitor, workingCopyManager);
    return op.getDocument().get();
  }

  protected void organizeImports(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    OrganizeImportOperation op = new OrganizeImportOperation(m_createdCompilationUnit);
    op.validate();
    op.run(monitor, workingCopyManager);
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    SubMonitor progress = SubMonitor.convert(monitor, getOperationName(), 4);

    try {
      String newSource = getSourceFormatted(getContent(), m_project, progress.newChild(0), workingCopyManager);
      if (progress.isCanceled()) {
        return;
      }
      progress.worked(1);

      IPackageFragment pck = getSourceFolder().getPackageFragment(getPackageName());
      if (!pck.exists()) {
        pck = getSourceFolder().createPackageFragment(getPackageName(), true, progress.newChild(0));
      }
      progress.worked(1);

      m_createdCompilationUnit = pck.getCompilationUnit(getFileName());
      if (!m_createdCompilationUnit.exists()) {
        m_createdCompilationUnit = pck.createCompilationUnit(getFileName(), newSource, true, progress.newChild(0));
        progress.worked(1);

        workingCopyManager.register(m_createdCompilationUnit, progress.newChild(0));
        organizeImports(monitor, workingCopyManager);
        progress.worked(1);
      }
      else {
        // only write if changed
        String oldSource = S2eUtils.getContentOfFile((IFile) m_createdCompilationUnit.getResource());
        progress.worked(1);

        if (!Objects.equals(oldSource, newSource)) {
          workingCopyManager.register(m_createdCompilationUnit, progress.newChild(0));

          IBuffer buffer = m_createdCompilationUnit.getBuffer();
          buffer.setContents(newSource);
          organizeImports(monitor, workingCopyManager);
        }
        progress.worked(1);
      }
    }
    catch (Exception e) {
      SdkLog.error("Could not {}", getOperationName(), e);
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

  public String getContent() {
    return m_content;
  }

  @Override
  public IFile getFile() {
    if (!S2eUtils.exists(m_createdCompilationUnit)) {
      return null;
    }
    IResource resource = m_createdCompilationUnit.getResource();
    if (resource == null || !resource.exists() || resource.getType() != IResource.FILE) {
      return null;
    }
    return (IFile) resource;
  }
}
