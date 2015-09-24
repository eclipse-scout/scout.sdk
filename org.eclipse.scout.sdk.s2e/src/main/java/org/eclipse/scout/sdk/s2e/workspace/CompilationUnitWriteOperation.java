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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

import org.apache.commons.lang3.Validate;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.core.util.CompositeObject;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.log.ScoutStatus;
import org.eclipse.scout.sdk.s2e.util.JdtUtils;

/**
 * <h3>{@link CompilationUnitWriteOperation}</h3>
 * <p>
 * Change the content of a new or existing compilation unit
 * <p>
 * In order to save memory, the content is outsourced to the filesystem until it is effectively used
 *
 * @author imo
 * @since 5.1.0
 */
public class CompilationUnitWriteOperation implements IWorkspaceBlockingOperation {
  private final ICompilationUnit m_cu;
  private File m_tmpFile;

  public CompilationUnitWriteOperation(IType existingJdtType, String content) throws CoreException {
    if (existingJdtType == null) {
      throw new IllegalArgumentException("existingJdtType is null");
    }
    m_cu = existingJdtType.getCompilationUnit();
    m_tmpFile = JdtUtils.writeTempFile("eclipse-cu", ".java", content);
  }

  public CompilationUnitWriteOperation(ICompilationUnit existingUnit, String content) throws CoreException {
    if (existingUnit == null) {
      throw new IllegalArgumentException("existingUnit is null");
    }
    m_cu = existingUnit;
    m_tmpFile = JdtUtils.writeTempFile("eclipse-cu", ".java", content);
  }

  public CompilationUnitWriteOperation(IPackageFragment pck, String fileName, String content) throws CoreException {
    if (pck == null) {
      throw new IllegalArgumentException("package is null");
    }
    IFolder folder = (IFolder) pck.getResource();
    IFile file = folder.getFile(fileName);
    m_cu = JavaCore.createCompilationUnitFrom(file);
    m_tmpFile = JdtUtils.writeTempFile("eclipse-cu", ".java", content);
  }

  public CompilationUnitWriteOperation(IPackageFragmentRoot srcFolder, String packageName, String fileName, String content) throws CoreException {
    if (srcFolder == null) {
      throw new IllegalArgumentException("srcFolder is null");
    }
    IFolder folder = (IFolder) (packageName != null ? srcFolder.getPackageFragment(packageName).getResource() : srcFolder.getResource());
    IFile file = folder.getFile(fileName);
    m_cu = JavaCore.createCompilationUnitFrom(file);
    m_tmpFile = JdtUtils.writeTempFile("eclipse-cu", ".java", content);
  }

  @Override
  protected void finalize() throws Throwable {
    m_tmpFile.delete();
    super.finalize();
  }

  @Override
  public String getOperationName() {
    return "Change " + m_cu.getPath();
  }

  public ICompilationUnit getCompilationUnit() {
    return m_cu;
  }

  @Override
  public void validate() {
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    Validate.notNull(workingCopyManager);
    monitor.subTask("write " + m_cu.getPath());

    // format source
    SourceFormatOperation op = new SourceFormatOperation(m_cu.getJavaProject(), new Document(JdtUtils.readTempFile(m_tmpFile)), null);
    op.validate();
    op.run(monitor, null);
    String newSource = op.getDocument().get();
    if (monitor.isCanceled()) {
      return;
    }

    // compare
    String oldSource = getExistingContent();
    if (isSourceEquals(oldSource, newSource)) {
      oldSource = null;
      return;
    }
    oldSource = null;

    if (monitor.isCanceled()) {
      return;
    }

    // write new source
    try {
      m_cu.becomeWorkingCopy(monitor);

      // store new form data content to buffer
      m_cu.getBuffer().setContents(newSource);

      // save buffer
      m_cu.getBuffer().save(monitor, true);

      m_cu.commitWorkingCopy(true, monitor);
    }
    catch (Exception e) {
      S2ESdkActivator.logError("could not store '" + m_cu.getPath() + "'.", e);
    }
    finally {
      m_cu.discardWorkingCopy();
    }
  }

  protected String getExistingContent() throws CoreException {
    IFile targetFile = (IFile) m_cu.getResource();
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

  private static boolean isSourceEquals(String source1, String source2) {
    if (source1 == null && source2 == null) {
      return true;
    }
    else if (source1 == null) {
      return false;
    }
    else if (source2 == null) {
      return false;
    }
    if (source1.length() != source2.length()) {
      return false;
    }
    return source1.equals(source2);
  }

  public static IPackageFragmentRoot findPrimarySourceFolder(IJavaProject project) throws JavaModelException {
    TreeMap<CompositeObject, IPackageFragmentRoot> prioMap = new TreeMap<>();
    for (IPackageFragmentRoot root : project.getPackageFragmentRoots()) {
      if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
        String s = root.getPath().removeFirstSegments(1).toString();
        if (s.equals("src/main/java")) {
          prioMap.put(new CompositeObject(10, root), root);
        }
        else if (s.equals("src")) {
          prioMap.put(new CompositeObject(11, root), root);
        }
        else if (s.startsWith("src/main/")) {
          prioMap.put(new CompositeObject(12, root), root);
        }
        else if (s.equals("src/test/java")) {
          prioMap.put(new CompositeObject(20, root), root);
        }
        else if (s.startsWith("src/test/")) {
          prioMap.put(new CompositeObject(21, root), root);
        }
        else {
          prioMap.put(new CompositeObject(30, root), root);
        }
      }
    }
    return prioMap.isEmpty() ? null : prioMap.firstEntry().getValue();
  }

  public static IPackageFragmentRoot findSourceFolder(IJavaElement e) {
    return (IPackageFragmentRoot) e.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
  }

}
