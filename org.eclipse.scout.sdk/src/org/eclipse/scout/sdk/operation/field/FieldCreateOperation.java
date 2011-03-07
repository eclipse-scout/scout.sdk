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
package org.eclipse.scout.sdk.operation.field;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ScoutSdkUtility;
import org.eclipse.scout.sdk.jdt.signature.CompilationUnitImportValidator;
import org.eclipse.scout.sdk.jdt.signature.IImportValidator;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.annotation.AnnotationCreateOperation;
import org.eclipse.scout.sdk.operation.util.SourceFormatOperation;
import org.eclipse.scout.sdk.typecache.IScoutWorkingCopyManager;

/**
 *
 */
public class FieldCreateOperation implements IOperation {

  private final IType m_declaringType;
  private final boolean m_formatSource;
  private final String m_name;
  private IJavaElement m_sibling;
  private String m_signature;
  private int m_flags;
  private String m_simpleInitValue;
  private List<AnnotationCreateOperation> m_annotations = new ArrayList<AnnotationCreateOperation>(3);
  // out
  private IField m_createdField;

  public FieldCreateOperation(IType declaringType, String fieldName, boolean formatSource) {
    m_declaringType = declaringType;
    m_name = fieldName;
    m_formatSource = formatSource;
  }

  @Override
  public String getOperationName() {
    return "create field '" + getName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getDeclaringType() == null) {
      throw new IllegalArgumentException("declaring type can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getName())) {
      throw new IllegalArgumentException("field name can not be null or empty.");
    }
    if (StringUtility.isNullOrEmpty(getSignature())) {
      throw new IllegalArgumentException("field signature can not be null or empty.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IScoutWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    workingCopyManager.register(getDeclaringType().getCompilationUnit(), monitor);
    // find sibling
    CompilationUnitImportValidator validator = new CompilationUnitImportValidator(getDeclaringType().getCompilationUnit());
    StringBuilder builder = new StringBuilder();
    buildSource(builder, validator);
    // create imports
    for (String fqi : validator.getImportsToCreate()) {
      getDeclaringType().getCompilationUnit().createImport(fqi, null, monitor);
    }
    Document sourceDocument = new Document(builder.toString());
    if (isFormatSource()) {
      SourceFormatOperation op = new SourceFormatOperation(getDeclaringType().getJavaProject(), sourceDocument, null);
      op.validate();
      op.run(monitor, workingCopyManager);
    }
    m_createdField = getDeclaringType().createField(sourceDocument.get(), getSibling(), true, monitor);
  }

  public void buildSource(StringBuilder builder, IImportValidator validator) throws JavaModelException {
    AnnotationCreateOperation[] annotations = getAnnotations();
    if (annotations != null && annotations.length > 0) {
      for (int i = 0; i < annotations.length; i++) {
        builder.append(annotations[i].createSource(validator, "\n"));
        builder.append("\n");
      }
    }
    if (Flags.isPublic(getFlags())) {
      builder.append("public ");
    }
    else if (Flags.isProtected(getFlags())) {
      builder.append("protected ");
    }
    else if (Flags.isPackageDefault(getFlags())) {
      builder.append("public ");
    }
    else if (Flags.isPrivate(getFlags())) {
      builder.append("private ");
    }
    if (Flags.isStatic(getFlags())) {
      builder.append("static ");
    }
    if (Flags.isFinal(getFlags())) {
      builder.append("final ");
    }

    // field type
    builder.append(ScoutSdkUtility.getSimpleTypeRefName(getSignature(), validator) + " ");
    // name
    builder.append(getName());

    // init value
    String body = createInitValue(validator);
    if (body != null) {
      builder.append(body);
    }
    builder.append(";");
  }

  /**
   * can be overridden to provide a specific method body. The method body is defined as part between the method body{}.
   * Use {@link ScoutSdkUtility#getSimpleTypeRefName(String, IImportValidator)} to determ class references (fully
   * quallified vs. simple name).
   * 
   * @param validator
   *          validator can be used to determ class references (fully quallified vs. simple name).
   * @return
   * @throws JavaModelException
   */
  protected String createInitValue(@SuppressWarnings("unused") IImportValidator validator) throws JavaModelException {
    if (!StringUtility.isNullOrEmpty(getSimpleInitValue())) {
      return " = " + getSimpleInitValue();
    }
    else {
      return null;
    }
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  public IField getCreatedField() {
    return m_createdField;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public String getName() {
    return m_name;
  }

  public void setSibling(IJavaElement sibling) {
    m_sibling = sibling;
  }

  public IJavaElement getSibling() {
    return m_sibling;
  }

  public void setSignature(String signature) {
    m_signature = signature;
  }

  public String getSignature() {
    return m_signature;
  }

  public void setFlags(int flags) {
    m_flags = flags;
  }

  public int getFlags() {
    return m_flags;
  }

  public void addAnnotation(AnnotationCreateOperation op) {
    m_annotations.add(op);
  }

  public AnnotationCreateOperation[] getAnnotations() {
    return m_annotations.toArray(new AnnotationCreateOperation[m_annotations.size()]);
  }

  public void setSimpleInitValue(String simpleInitValue) {
    m_simpleInitValue = simpleInitValue;
  }

  public String getSimpleInitValue() {
    return m_simpleInitValue;
  }

}
