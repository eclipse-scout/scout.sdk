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

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.nls.DynamicNls;
import org.eclipse.scout.nls.sdk.internal.model.workspace.manifest.ManifestElement;
import org.eclipse.scout.nls.sdk.internal.model.workspace.manifest.ManifestEntry;
import org.eclipse.scout.nls.sdk.internal.model.workspace.nlsfile.AbstractNlsFile;
import org.eclipse.scout.nls.sdk.model.workspace.util.PropertyFileReader;
import org.eclipse.scout.nls.sdk.operations.desc.NewNlsFileOperationDesc;

public class CreateNlsProjectOperationDynamic extends AbstractCreateNlsProjectOperation {

  private static final String NL = System.getProperty("line.separator");

  public CreateNlsProjectOperationDynamic(NewNlsFileOperationDesc desc) {
    super(desc);
  }

  @Override
  public List<ManifestEntry> getManifestEntries() {
    List<ManifestEntry> entries = new LinkedList<ManifestEntry>();
    ManifestEntry entry = new ManifestEntry("Export-Package");
    ManifestElement element = new ManifestElement(getDesc().getPackage());
    entry.addElement(element);
    entries.add(entry);
    // add parent plugin dependency
    if (getDesc().getParentPlugin() != null) {
      entry = new ManifestEntry("Require-Bundle");
      element = new ManifestElement(getDesc().getParentPlugin().getBundleDescription().getName());
      element.addProperty("visibility:", "reexport");
      entry.addElement(element);
      entries.add(entry);
    }

    return entries;
  }

  @Override
  protected byte[] getClassContent() throws JavaModelException {
    NewNlsFileOperationDesc desc = getDesc();
    String className = desc.getClassName();
    IType parentType = null;
    if (desc.getParentFile() != null) {
      PropertyFileReader parentProperties = new PropertyFileReader(desc.getParentFile());
      String parentClass = parentProperties.getAttribute(AbstractNlsFile.MANIFEST_CLASS);
      IJavaProject jp = JavaCore.create(desc.getParentFile().getProject());
      parentType = jp.findType(parentClass);
    }
    String resourcePathString = desc.getTranslationFolder();
    resourcePathString = resourcePathString.replace("/", ".");
    resourcePathString = resourcePathString + "." + desc.getTranlationFileName();
    if (resourcePathString.endsWith(".properties")) {
      resourcePathString = resourcePathString.substring(0, resourcePathString.length() - ".properties".length());
    }
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
    writer.append("  private static String RESOURCE_BUNDLE_NAME = \"" + resourcePathString + "\"; //$NON-NLS-1$" + NL);
    writer.append("  private static " + className + " instance = new " + className + "();" + NL);
    writer.append(NL);
    writer.append("  public static " + className + " getInstance() {" + NL);
    writer.append("    return instance;" + NL);
    writer.append("  }" + NL);
    writer.append(NL);
    writer.append("  private " + className + "() {" + NL);
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

  @Override
  protected byte[] getDefaultMessagesFileContent() {
    StringWriter writer = new StringWriter();
    writer.append(getTranslationFileHeader(getDesc().getFileName() + ".nls"));
    return writer.toString().getBytes();
  }

}
