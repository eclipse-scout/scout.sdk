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

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.nls.sdk.internal.model.workspace.manifest.ManifestEntry;
import org.eclipse.scout.nls.sdk.operations.desc.NewNlsFileOperationDesc;

public class CreateNlsProjectOperationStatic extends AbstractCreateNlsProjectOperation {

  public CreateNlsProjectOperationStatic(NewNlsFileOperationDesc desc) {
    super(desc);
  }

  @Override
  public List<ManifestEntry> getManifestEntries() {
    List<ManifestEntry> entries = new LinkedList<ManifestEntry>();
    return entries;
  }

  @Override
  protected byte[] getClassContent() throws JavaModelException {
    NewNlsFileOperationDesc desc = getDesc();
    String className = desc.getClassName();
    // IType parentType = null;
    // if (desc.getParentFile() != null) {
    // PropertyFileReader parentProperties = new PropertyFileReader(desc.getParentFile());
    // // String parentClass = parentProperties.getAttribute(AbstractNlsFile.MANIFEST_CLASS);
    // IJavaProject jp = JavaCore.create(desc.getParentFile().getProject());
    // // parentType = jp.findType(parentClass);
    // }
    StringWriter writer = new StringWriter();
    writer.append("package " + desc.getPackage() + ";" + NL);
    writer.append(NL);
    writer.append("import org.eclipse.osgi.util.NLS;" + NL);
    writer.append(NL);
    writer.append(getNlsClassFileHeader(desc.getFileName() + ".nls"));
    writer.append("public class " + className + " {" + NL);
    String resourcePathString = desc.getTranslationFolder();
    resourcePathString = resourcePathString.replace("/", ".");
    resourcePathString = resourcePathString + "." + desc.getTranlationFileName();
    if (resourcePathString.endsWith(".properties")) {
      resourcePathString = resourcePathString.substring(0, resourcePathString.length() - ".properties".length());
    }
    writer.append("  private static final String BUNDLE_NAME = \"" + resourcePathString + "\"; //$NON-NLS-1$" + NL);
    writer.append(NL);
    writer.append("  static {" + NL);
    writer.append("    // initialize resource bundle" + NL);
    writer.append("    NLS.initializeMessages(BUNDLE_NAME, " + className + ".class);" + NL);
    writer.append("  }" + NL);
    writer.append(NL);
    writer.append("  private " + className + "() {" + NL);
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
