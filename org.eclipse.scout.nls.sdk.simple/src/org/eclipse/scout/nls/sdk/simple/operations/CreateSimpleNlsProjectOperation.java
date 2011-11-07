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
package org.eclipse.scout.nls.sdk.simple.operations;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.scout.commons.nls.DynamicNls;
import org.eclipse.scout.nls.sdk.NlsCore;
import org.eclipse.scout.nls.sdk.internal.jdt.NlsJdtUtility;
import org.eclipse.scout.nls.sdk.pde.PluginModelModificationHelper;
import org.eclipse.scout.nls.sdk.simple.model.ws.NlsType;
import org.eclipse.scout.nls.sdk.simple.model.ws.nlsfile.AbstractNlsFile;

public class CreateSimpleNlsProjectOperation extends AbstractCreateNlsProjectOperation {

  public CreateSimpleNlsProjectOperation(NewNlsFileOperationDesc desc) {
    super(desc);
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    try {
      // ensure sync
      if (!getDesc().getPlugin().isSynchronized(IResource.DEPTH_INFINITE)) {
        getDesc().getPlugin().refreshLocal(IResource.DEPTH_INFINITE, monitor);
      }

      try {
        PluginModelModificationHelper ed = new PluginModelModificationHelper(getDesc().getPlugin());
        ed.addDependency(NLS_RUNNTIME_PLUGIN, true);
        if (getDesc().getParentPlugin() != null && getDesc().getParentFile() != null) {
          ed.addDependency(getDesc().getParentPlugin().getBundleDescription().getName());
        }
        ed.addExportPackage(getDesc().getPackage());
        ed.save();
      }
      catch (Exception e1) {
        throw new CoreException(new Status(IStatus.ERROR, NlsCore.PLUGIN_ID, 0, "Unable to edit manifest of project " + getDesc().getPlugin().getName(), e1));
      }

      // create Texts class file
      createJavaClass(monitor);

      // create language translation properties files
      createLanguageFiles(monitor);

      // create .nls file
      createNlsFile(monitor);
    }
    catch (CoreException e) {
      NlsCore.logError("could not create NLS project", e);
    }
    return Status.OK_STATUS;
  }

  private byte[] getNlsFileContent() {
    StringWriter writer = new StringWriter();
    writer.append(getNlsFileHeader());
    writer.append(AbstractNlsFile.MANIFEST_CLASS + "=" + getDesc().getPackage() + "." + getDesc().getClassName() + NL);
    return writer.toString().getBytes();
  }

  private void createLanguageFiles(IProgressMonitor monitor) throws CoreException {
    IFolder folder = getDesc().getPlugin().getFolder(getDesc().getTranslationFolder());
    createLanguageFile(null, folder, getDesc().getFileName(), monitor);
  }

  private static final String getNlsClassFileHeader(String translationFileName) {
    StringBuilder builder = new StringBuilder();
    builder.append("/**" + NL);
    builder.append(" * This class provides the NLS support." + NL);
    builder.append(" */" + NL);
    return builder.toString();
  }

  private static final String getNlsFileHeader() {
    StringBuilder builder = new StringBuilder();
    builder.append("################################################" + NL);
    builder.append("# This file is maintained by the nls editor.   #" + NL);
    builder.append("# To ensure a properly working nls support     #" + NL);
    builder.append("# keep this file untouched.                    #" + NL);
    builder.append("################################################" + NL);
    return builder.toString();
  }

  private static final String getTranslationFileHeader() {
    StringBuilder builder = new StringBuilder();
    builder.append("##############################################################" + NL);
    builder.append("# This file is maintained by the NLS project and should not  #" + NL);
    builder.append("# be modified directly. Use the NLS Editor to add, remove or #" + NL);
    builder.append("# change translations.                                       #" + NL);
    builder.append("##############################################################" + NL);
    return builder.toString();
  }

  public static void createLanguageFile(String lang, IFolder folder, String filePrefix, IProgressMonitor monitor) throws CoreException {
    NlsJdtUtility.createFolder(folder, true, monitor);
    if (lang == null) lang = "";
    else lang = "_" + lang;
    String filename = filePrefix + lang + ".properties";
    IFile file = folder.getFile(filename);
    if (!file.exists()) {
      file.create(new ByteArrayInputStream(getDefaultMessagesFileContent()), true, monitor);
    }
  }

  private final void createNlsFile(IProgressMonitor monitor) throws CoreException {
    if (getDesc().getPlugin() != null) {
      IFile file = getDesc().getPlugin().getFile(getDesc().getFileName() + ".nls");
      if (!file.exists()) {
        file.create(new ByteArrayInputStream(getNlsFileContent()), true, monitor);
      }
    }
  }

  private final void createJavaClass(IProgressMonitor monitor) throws CoreException {
    createJavaClass(getDesc().getPlugin(),
        getDesc().getSourceContainer(),
        getDesc().getPackage(),
        getDesc().getClassName(),
        getClassContent(),
        monitor);
  }

  public static String getResourcePathString(String folder, String filePrefix) {
    String resourcePathString = folder;
    resourcePathString = resourcePathString.replace("/", ".");
    resourcePathString = resourcePathString + "." + filePrefix;
    if (resourcePathString.endsWith(".properties")) {
      resourcePathString = resourcePathString.substring(0, resourcePathString.length() - ".properties".length());
    }
    return resourcePathString;
  }

  private byte[] getClassContent() throws CoreException {
    NewNlsFileOperationDesc desc = getDesc();
    String className = desc.getClassName();
    IType parentType = null;
    if (desc.getParentFile() != null) {
      Properties parentProperties = new Properties();
      try {
        parentProperties.load(desc.getParentFile().getContents());
      }
      catch (Exception e) {
        throw new CoreException(new Status(IStatus.ERROR, NlsCore.PLUGIN_ID, 0, "Unable to load parent nls file. ", e));
      }
      String parentClass = parentProperties.getProperty(AbstractNlsFile.MANIFEST_CLASS);
      IJavaProject jp = JavaCore.create(desc.getParentFile().getProject());
      parentType = jp.findType(parentClass);
    }

    String resourcePathString = getResourcePathString(getDesc().getTranslationFolder(), getDesc().getFileName());

    StringWriter writer = new StringWriter();
    writer.append("package " + desc.getPackage() + ";" + NL);
    writer.append(NL);
    writer.append("import java.util.Locale;" + NL);
    writer.append(NL);
    writer.append(getNlsClassFileHeader(desc.getFileName() + ".nls"));
    writer.append("public class " + className + " extends ");
    if (parentType != null) {
      writer.append(parentType.getFullyQualifiedName() + " {" + NL);
    }
    else {
      writer.append(DynamicNls.class.getName() + " {" + NL);
    }
    writer.append("  private static String " + NlsType.RESOURCE_BUNDLE_FIELD_NAME + " = \"" + resourcePathString + "\"; //$NON-NLS-1$" + NL);
    writer.append("  private static " + className + " instance = new " + className + "();" + NL);
    writer.append(NL);
    writer.append("  public static " + className + " getInstance() {" + NL);
    writer.append("    return instance;" + NL);
    writer.append("  }" + NL);
    writer.append(NL);
    writer.append("  protected " + className + "() {" + NL);
    writer.append("    registerResourceBundle(RESOURCE_BUNDLE_NAME, " + className + ".class);" + NL);
    writer.append("  }" + NL);
    writer.append(NL);
    writer.append("  public static String get(String key, String... messageArguments){" + NL);
    writer.append("    return getInstance().getText(key, messageArguments);" + NL);
    writer.append("  }" + NL);
    writer.append(NL);
    writer.append("  public static String get(Locale locale, String key, String... messageArguments){" + NL);
    writer.append("    return getInstance().getText(locale, key, messageArguments);" + NL);
    writer.append("  }" + NL);
    writer.append("}" + NL);
    return writer.toString().getBytes();
  }

  private static byte[] getDefaultMessagesFileContent() {
    StringWriter writer = new StringWriter();
    writer.append(getTranslationFileHeader());
    return writer.toString().getBytes();
  }
}
