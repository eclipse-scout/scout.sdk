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
package org.eclipse.scout.sdk.s2e.internal.dto;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.sdk.core.model.IType;
import org.eclipse.scout.sdk.core.util.CoreUtils;
import org.eclipse.scout.sdk.s2e.IOperation;
import org.eclipse.scout.sdk.s2e.IWorkingCopyManager;
import org.eclipse.scout.sdk.s2e.dto.IDtoAutoUpdateOperation;
import org.eclipse.scout.sdk.s2e.internal.S2ESdkActivator;
import org.eclipse.scout.sdk.s2e.job.OperationJob;
import org.eclipse.scout.sdk.s2e.log.ScoutStatus;
import org.eclipse.scout.sdk.s2e.operation.OrganizeImportOperation;
import org.eclipse.scout.sdk.s2e.operation.SourceFormatOperation;

/**
 *
 */
public abstract class AbstractDtoAutoUpdateOperation implements IDtoAutoUpdateOperation {
  private final IType m_modelType;
  private final IProject m_modelProject;
  private IFile m_derivedFile; // lazy loaded

  public AbstractDtoAutoUpdateOperation(IType modelType, IProject modelProject) {
    m_modelType = modelType;
    m_modelProject = modelProject;
  }

  @Override
  public String getOperationName() {
    return "Update DTO for '" + getModelType().getName() + "'.";
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof AbstractDtoAutoUpdateOperation)) {
      return false;
    }
    return m_modelType.equals(((AbstractDtoAutoUpdateOperation) obj).m_modelType);
  }

  @Override
  public int hashCode() {
    return m_modelType.hashCode();
  }

  @Override
  public void validate() {
    if (getModelType() == null) {
      throw new IllegalArgumentException("model type must exist: [" + getModelType() + "]");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    String backup = CoreUtils.getUsername();
    try {
      CoreUtils.setUsernameForThread("Scout robot");
      runImpl(monitor, workingCopyManager);
    }
    finally {
      CoreUtils.setUsernameForThread(backup);
    }
  }

  protected void runImpl(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    String newSource = createDerivedTypeSource();
    if (newSource == null || monitor.isCanceled()) {
      return;
    }

    // format source
    IFile targetFile = getDerivedFile();
    IJavaProject derivedTypeProject = JavaCore.create(targetFile.getProject());
    SourceFormatOperation op = new SourceFormatOperation(derivedTypeProject, new Document(newSource), null);
    op.validate();
    op.run(monitor, null);
    newSource = op.getDocument().get();
    if (monitor.isCanceled()) {
      return;
    }

    // compare
    String oldSource = getDerivedFileContent();
    if (!isSourceEquals(oldSource, newSource)) {
      oldSource = null;

      // write source
      P_FormDataStoreOperation storeOp = new P_FormDataStoreOperation(targetFile, newSource);
      if (monitor.isCanceled()) {
        return;
      }

      if (workingCopyManager != null) {
        storeOp.run(new NullProgressMonitor(), workingCopyManager);
      }
      else {
        OperationJob job = new OperationJob(storeOp);
        job.schedule();
      }
    }
  }

  private String getDerivedFileContent() throws CoreException {
    IFile targetFile = getDerivedFile();
    String charsetName = targetFile.getCharset();
    if (!Charset.isSupported(charsetName)) {
      throw new CoreException(new ScoutStatus("Charset '" + charsetName + "' is not supported."));
    }

    try (InputStream is = targetFile.getContents()) {
      return IOUtils.toString(is, Charset.forName(charsetName));
    }
    catch (IOException e) {
      throw new CoreException(new ScoutStatus("Unable to read file '" + targetFile.getFullPath().toOSString() + "'.", e));
    }
  }

  protected String getDerivedFileLineSeparator() throws CoreException {
    IScopeContext[] scopeContext = new IScopeContext[]{new ProjectScope(getDerivedFile().getProject())};
    String lineSeparator = Platform.getPreferencesService().getString(Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, "\n", scopeContext);
    return lineSeparator;
  }

  protected abstract String createDerivedTypeSource() throws CoreException;

  protected abstract String getDerivedTypeFqn();

  protected synchronized IFile getDerivedFile() throws CoreException {
    if (m_derivedFile == null) {
      IJavaProject derivedProject = JavaCore.create(getModelProject());
      String derivedTypeFqn = getDerivedTypeFqn();
      org.eclipse.jdt.core.IType derivedTypeJdt = derivedProject.findType(derivedTypeFqn);
      if (derivedTypeJdt == null) {
        throw new CoreException(new ScoutStatus("Unable to find the derived type '" + derivedTypeFqn + "'."));
      }
      IResource dtoFile = derivedTypeJdt.getResource();
      if (dtoFile == null || dtoFile.getType() != IResource.FILE || !dtoFile.exists()) {
        throw new CoreException(new ScoutStatus("Type '" + derivedTypeFqn + "' could not be found in the workspace."));
      }
      m_derivedFile = (IFile) dtoFile;
    }
    return m_derivedFile;
  }

  @Override
  public IProject getModelProject() {
    return m_modelProject;
  }

  @Override
  public IType getModelType() {
    return m_modelType;
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

  private static class P_FormDataStoreOperation implements IOperation {
    private final String m_icuSource;
    private final IFile m_derivedFile;

    public P_FormDataStoreOperation(IFile derivedFile, String icuSource) {
      m_derivedFile = derivedFile;
      m_icuSource = icuSource;
    }

    @Override
    public String getOperationName() {
      return "Update form data '" + m_derivedFile.getName() + "'.";
    }

    @Override
    public void validate() {
    }

    @Override
    public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
      ICompilationUnit icu = JavaCore.createCompilationUnitFrom(m_derivedFile);
      try {
        icu.becomeWorkingCopy(monitor);

        // store new form data content to buffer
        icu.getBuffer().setContents(m_icuSource);

        // save buffer
        icu.getBuffer().save(monitor, true);

        // organize import required to ensure the JDT settings for the imports are applied
        OrganizeImportOperation o = new OrganizeImportOperation(icu);
        o.validate();
        o.run(monitor, workingCopyManager);

        icu.commitWorkingCopy(true, monitor);
      }
      catch (Exception e) {
        S2ESdkActivator.logError("could not store DTO '" + m_derivedFile.getName() + "'.", e);
      }
      finally {
        icu.discardWorkingCopy();
      }
    }
  }
}
