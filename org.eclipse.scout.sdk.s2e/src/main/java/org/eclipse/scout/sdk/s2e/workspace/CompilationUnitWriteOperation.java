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
package org.eclipse.scout.sdk.s2e.workspace;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.Validate;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.core.util.SdkLog;
import org.eclipse.scout.sdk.s2e.log.ScoutStatus;

/**
 * <h3>{@link CompilationUnitWriteOperation}</h3>
 * <p>
 * Change the content of a new or existing compilation unit
 *
 * @author Ivan Motsch
 * @since 5.1.0
 */
public class CompilationUnitWriteOperation implements IOperation {
  private final ICompilationUnit m_cu;
  private final String m_content;

  public CompilationUnitWriteOperation(IType existingJdtType, String content) {
    if (existingJdtType == null) {
      m_cu = null;
    }
    else {
      m_cu = existingJdtType.getCompilationUnit();
    }
    m_content = content;
  }

  public CompilationUnitWriteOperation(ICompilationUnit existingUnit, String content) {
    m_cu = existingUnit;
    m_content = content;
  }

  public CompilationUnitWriteOperation(IPackageFragment pck, String fileName, String content) {
    if (pck == null) {
      m_cu = null;
    }
    else {
      IFolder folder = (IFolder) pck.getResource();
      IFile file = folder.getFile(fileName);
      m_cu = JavaCore.createCompilationUnitFrom(file);
    }
    m_content = content;
  }

  public CompilationUnitWriteOperation(IPackageFragmentRoot srcFolder, String packageName, String fileName, String content) {
    if (srcFolder == null) {
      m_cu = null;
    }
    else {
      IFolder folder = (IFolder) (packageName != null ? srcFolder.getPackageFragment(packageName).getResource() : srcFolder.getResource());
      IFile file = folder.getFile(fileName);
      m_cu = JavaCore.createCompilationUnitFrom(file);
    }
    m_content = content;
  }

  @Override
  public String getOperationName() {
    return "Write " + m_cu.getPath();
  }

  public ICompilationUnit getCompilationUnit() {
    return m_cu;
  }

  @Override
  public void validate() {
    if (m_cu == null) {
      throw new IllegalArgumentException("Compilation unit is null");
    }
    if (m_content == null) {
      throw new IllegalArgumentException("Compilation unit content cannot be null");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) {
    Validate.notNull(workingCopyManager);
    monitor.subTask("Write " + m_cu.getPath());

    try {
      // format source
      SourceFormatOperation op = new SourceFormatOperation(m_cu.getJavaProject(), new Document(m_content));
      op.validate();
      op.run(monitor, workingCopyManager);
      String newSource = op.getDocument().get();
      if (monitor.isCanceled()) {
        return;
      }

      // compare
      String oldSource = getContentOfFile((IFile) m_cu.getResource());
      if (isSourceEqual(oldSource, newSource)) {
        oldSource = null;
        return;
      }
      oldSource = null;

      if (monitor.isCanceled()) {
        return;
      }

      // write new source
      workingCopyManager.register(m_cu, monitor);

      // store new form data content to buffer
      IBuffer buffer = m_cu.getBuffer();
      buffer.setContents(newSource);
    }
    catch (Exception e) {
      SdkLog.error("Could not store '" + m_cu.getPath() + "'.", e);
    }
  }

  protected static String getContentOfFile(IFile targetFile) throws CoreException {
    if (!targetFile.exists()) {
      return null;
    }
    String charsetName = targetFile.getCharset();
    try (InputStream contents = targetFile.getContents()) {
      return CoreUtils.inputStreamToString(contents, charsetName).toString();
    }
    catch (IOException e) {
      throw new CoreException(new ScoutStatus("Unable to read file '" + targetFile.getFullPath().toOSString() + "'.", e));
    }
  }

  protected static boolean isSourceEqual(String source1, String source2) {
    if (source1 == null && source2 == null) {
      return true;
    }
    else if (source1 == null) {
      return false;
    }
    else if (source2 == null) {
      return false;
    }

    // only compare contents starting from the package declaration
    // ignore file headers in comparing the content
    source1 = getSourceStartingAtPackage(source1);
    source2 = getSourceStartingAtPackage(source2);
    if (source1.length() != source2.length()) {
      return false;
    }
    return source1.equals(source2);
  }

  protected static String getSourceStartingAtPackage(String fullSource) {
    int packagePos = fullSource.indexOf("package ");
    if (packagePos <= 0) {
      return fullSource;
    }
    return fullSource.substring(packagePos);
  }
}
