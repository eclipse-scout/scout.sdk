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
package org.eclipse.scout.nls.sdk.operations;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.jdt.NlsJdtHandler;
import org.eclipse.scout.nls.sdk.internal.model.workspace.manifest.ManifestElement;
import org.eclipse.scout.nls.sdk.internal.model.workspace.manifest.ManifestEntry;
import org.eclipse.scout.nls.sdk.internal.model.workspace.manifest.WorkspaceManifestReader;
import org.eclipse.scout.nls.sdk.operations.desc.NewNlsFileOperationDesc;

public abstract class AbstractCreateNlsProjectOperation extends Job {

  protected static final String NL = System.getProperty("line.separator");
  private static final String NLS_RUNNTIME_PLUGIN = "org.eclipse.scout.commons";

  private NewNlsFileOperationDesc m_desc;

  public AbstractCreateNlsProjectOperation(NewNlsFileOperationDesc desc) {
    super("create new Nls project...");
    m_desc = desc;
  }

  public final IStatus runSync() {
    return run(new NullProgressMonitor());
  }

  @Override
  protected final IStatus run(IProgressMonitor monitor) {
    try {
      // ensure sync
      if (!m_desc.getPlugin().isSynchronized(IResource.DEPTH_INFINITE)) {
        m_desc.getPlugin().refreshLocal(IResource.DEPTH_INFINITE, monitor);
      }
      WorkspaceManifestReader reader = new WorkspaceManifestReader(m_desc.getPlugin());
      ManifestEntry entry = new ManifestEntry("Require-Bundle");
      ManifestElement element = new ManifestElement(NLS_RUNNTIME_PLUGIN);
      element.addProperty("visibility:", "reexport");
      entry.addElement(element);
      reader.addElement(entry);
      for (ManifestEntry e : getManifestEntries()) {
        reader.addElement(e);
      }
      reader.store(monitor);
      // create class file
      createJavaClass(monitor);
      // create default message file
      createDefaultMessagesFile(monitor);
      // create nls file
      createNlsFile(monitor);

    }
    catch (CoreException e) {
      NlsCore.logError("could not create NLS project", e);
    }
    return Status.OK_STATUS;
  }

  protected NewNlsFileOperationDesc getDesc() {
    return m_desc;
  }

  protected final void createJavaClass(IProgressMonitor monitor) throws CoreException {
    IJavaProject jp = JavaCore.create(m_desc.getPlugin());
    // IPackageFragment frag = jp.findPackageFragment(m_desc.getSourceContainer().append(
    // m_desc.getPackage().replaceAll(".", "/")));
    IPackageFragmentRoot root = jp.findPackageFragmentRoot(m_desc.getSourceContainer());
    IPackageFragment fragment = root.createPackageFragment(m_desc.getPackage(), true, monitor);
    String className = m_desc.getClassName();
    if (!className.endsWith(".java")) {
      className = className + ".java";
    }
    ICompilationUnit unit = fragment.createCompilationUnit(className, "asdf", true, monitor);
    Assert.isTrue(unit.getUnderlyingResource() instanceof IFile);
    IFile classFile = (IFile) unit.getUnderlyingResource();
    classFile.setContents(new ByteArrayInputStream(getClassContent()), true, false, monitor);

  }

  protected final void createDefaultMessagesFile(IProgressMonitor monitor) throws CoreException {

    IFolder folder = m_desc.getPlugin().getFolder(m_desc.getTranslationFolder());
    NlsJdtHandler.createFolder(folder, true, monitor);
    IFile defaultFile = folder.getFile(m_desc.getTranlationFileName() + ".properties");
    if (!defaultFile.exists()) {
      defaultFile.create(new ByteArrayInputStream(getDefaultMessagesFileContent()), true, monitor);
    }
  }

  protected final void createNlsFile(IProgressMonitor monitor) throws CoreException {
    if (m_desc.getPlugin() != null) {

      IFile file = m_desc.getPlugin().getFile(m_desc.getFileName() + ".nls");
      if (!file.exists()) {
        file.create(new ByteArrayInputStream(getNlsFileContent()), true, monitor);
      }
    }

  }

  protected abstract List<ManifestEntry> getManifestEntries();

  protected abstract byte[] getClassContent() throws JavaModelException;

  protected abstract byte[] getDefaultMessagesFileContent();

  protected byte[] getNlsFileContent() {
    StringWriter writer = new StringWriter();
    writer.append(getNlsFileHeader());
    if (m_desc.isFileTypeDynamic()) {
      writer.append("Nls-Type=dynamic" + NL);
      if (m_desc.getParentPlugin() != null && m_desc.getParentFile() != null) {
        writer.append("Nls-Parent-File=" + m_desc.getParentPlugin().getBundleDescription().getName());
        writer.append(":" + m_desc.getParentFile().getName() + NL);
      }
    }
    writer.append("Nls-Class=" + m_desc.getPackage() + "." + m_desc.getClassName() + NL);
    writer.append("Nls-File-Prefix=" + m_desc.getTranlationFileName() + NL);
    writer.append("Nls-Translation-Folder=" + m_desc.getTranslationFolder() + NL);

    return writer.toString().getBytes();
  }

  public static final String getNlsFileHeader() {
    StringBuilder builder = new StringBuilder();
    builder.append("##################################################" + NL);
    builder.append("# This file is maintained by the nls editor      #" + NL);
    builder.append("# To ensure a properly working nls support of    #" + NL);
    builder.append("# keep this file untouched directly.             #" + NL);
    builder.append("##################################################" + NL);
    return builder.toString();
  }

  public static final String getTranslationFileHeader(String translationFileName) {
    StringBuilder builder = new StringBuilder();
    builder.append("##############################################################" + NL);
    builder.append("# This file is maintained by the " + translationFileName + " file and    #" + NL);
    builder.append("# should not be modified directly. Use the NLS Editor to     #" + NL);
    builder.append("# add,remove or change translations.                         #" + NL);
    builder.append("##############################################################" + NL);
    return builder.toString();
  }

  public static final String getNlsClassFileHeader(String translationFileName) {
    StringBuilder builder = new StringBuilder();
    builder.append("/**" + NL);
    builder.append(" * This class provides the nls support." + NL);
    builder.append(" * Do not change any member nor field of this class anytime otherwise the" + NL);
    builder.append(" * nls support is not anymore garanteed." + NL);
    builder.append(" * This calss is auto generated and is maintained by the plugins" + NL);
    builder.append(" * " + translationFileName + " file in the root directory of the plugin." + NL);
    builder.append(" * " + NL);
    builder.append(" * @see " + translationFileName + NL);
    builder.append(" */" + NL);
    return builder.toString();
  }
}
