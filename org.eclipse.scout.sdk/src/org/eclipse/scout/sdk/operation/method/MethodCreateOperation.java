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
package org.eclipse.scout.sdk.operation.method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.annotation.AnnotationCreateOperation;
import org.eclipse.scout.sdk.operation.util.SourceFormatOperation;
import org.eclipse.scout.sdk.util.ScoutUtility;
import org.eclipse.scout.sdk.util.signature.CompilationUnitImportValidator;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 *
 */
public class MethodCreateOperation implements IOperation {

  private final IType m_declaringType;
  private final String m_methodName;
  private boolean m_formatSource;
  private IJavaElement m_sibling;
  private int m_methodFlags;
  private String m_returnTypeSignature;
  private String[] m_parameterSignatures;
  private String[] m_parameterNames;
  private List<AnnotationCreateOperation> m_annotations = new ArrayList<AnnotationCreateOperation>();
  private List<String> m_exceptionSignatures = new ArrayList<String>();
  private IMethod m_createdMethod;
  private String m_simpleBody;

  public MethodCreateOperation(IType declaringType, String methodName) {
    this(declaringType, methodName, null);
  }

  public MethodCreateOperation(IType declaringType, String methodName, String simpleBody) {
    this(declaringType, methodName, simpleBody, false);
  }

  /**
   * @param declaringType
   *          the type holding the new method
   * @param methodName
   *          a method name.
   * @param simpleBody
   *          the method body without using any additional imports (e.g. "return null;"). If additional imports are
   *          used, override the createMethodBody method.
   */
  public MethodCreateOperation(IType declaringType, String methodName, String simpleBody, boolean formatSource) {
    m_declaringType = declaringType;
    m_methodName = methodName;
    m_simpleBody = simpleBody;
    m_formatSource = formatSource;
  }

  @Override
  public String getOperationName() {
    return "Create method '" + getMethodName() + "' in '" + getDeclaringType().getFullyQualifiedName() + "'";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getMethodName())) {
      throw new IllegalArgumentException("method name can not be null.");
    }
    if (getDeclaringType() == null) {
      throw new IllegalArgumentException("declaring type can not be null.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    workingCopyManager.register(getDeclaringType().getCompilationUnit(), monitor);
    CompilationUnitImportValidator validator = new CompilationUnitImportValidator(getDeclaringType().getCompilationUnit());
    StringBuilder builder = new StringBuilder();
    buildSource(builder, validator);
    // create imports
    for (String fqi : validator.getImportsToCreate()) {
      if (!fqi.matches("$" + getDeclaringType().getFullyQualifiedName() + "(\\.|\\$)[^.]*^")) {
        getDeclaringType().getCompilationUnit().createImport(fqi, null, monitor);
      }
    }
    Document sourceDocument = new Document(builder.toString());
    if (isFormatSource()) {
      SourceFormatOperation op = new SourceFormatOperation(getDeclaringType().getJavaProject(), sourceDocument, null);
      op.validate();
      op.run(monitor, workingCopyManager);
    }
    m_createdMethod = getDeclaringType().createMethod(sourceDocument.get(), getSibling(), true, monitor);
  }

  public void buildSource(StringBuilder builder, IImportValidator validator) throws JavaModelException {
    AnnotationCreateOperation[] annotations = getAnnotations();
    if (annotations != null && annotations.length > 0) {
      for (int i = 0; i < annotations.length; i++) {
        builder.append(annotations[i].createSource(validator, "\n"));
        builder.append("\n");
      }
    }
    if (Flags.isPublic(getMethodFlags())) {
      builder.append("public ");
    }
    else if (Flags.isProtected(getMethodFlags())) {
      builder.append("protected ");
    }
    else if (Flags.isPackageDefault(getMethodFlags())) {
      builder.append("public ");
    }
    if (Flags.isStatic(getMethodFlags())) {
      builder.append("static ");
    }
    // return value
    if (!StringUtility.isNullOrEmpty(getReturnTypeSignature())) {
      builder.append(SignatureUtility.getTypeReference(getReturnTypeSignature(), getDeclaringType(), validator) + " ");
    }
    builder.append(getMethodName() + "(");
    // parameters
    String[] parameterSignatures = getParameterSignatures();
    if (parameterSignatures != null && parameterSignatures.length > 0) {
      String[] parameterNames = getParameterNames();
      if (parameterNames == null || parameterNames.length != parameterSignatures.length) {
        // use arg1 ... argN
        parameterNames = new String[parameterSignatures.length];
        for (int i = 0; i < parameterNames.length; i++) {
          parameterNames[i] = "arg" + i;
        }
      }
      for (int i = 0; i < parameterSignatures.length; i++) {
        builder.append(SignatureUtility.getTypeReference(parameterSignatures[i], validator) + " ");
        builder.append(parameterNames[i]);
        if ((parameterSignatures.length - 1) > i) {
          builder.append(", ");
        }
      }
    }
    builder.append(") ");
    // exceptions
    String[] exceptionSignatures = getExceptionSignatures();
    if (exceptionSignatures != null && exceptionSignatures.length > 0) {

      for (int i = 0; i < exceptionSignatures.length; i++) {
        builder.append("throws " + SignatureUtility.getTypeReference(exceptionSignatures[i], validator));
        builder.append(" ");
      }
    }
    if (Flags.isInterface(getMethodFlags())) {
      builder.append(";");
    }
    else {
      // body
      builder.append("{\n");
      String body = createMethodBody(validator);
      if (body != null) {
        builder.append(body);
        builder.append("\n");
      }
      builder.append("}\n");
    }
  }

  /**
   * can be overridden to provide a specific method body. The method body is defined as part between the method body{}.
   * Use {@link SignatureUtility#getTypeReference(String, IImportValidator)} to determ class references (fully
   * quallified vs. simple name).
   * 
   * @param validator
   *          validator can be used to determ class references (fully quallified vs. simple name).
   * @return
   * @throws JavaModelException
   */
  protected String createMethodBody(IImportValidator validator) throws JavaModelException {
    StringBuilder builder = new StringBuilder();
    if (!StringUtility.isNullOrEmpty(getSimpleBody())) {
      builder.append(getSimpleBody());
    }
    else {
      builder.append(ScoutUtility.getCommentAutoGeneratedMethodStub());
      if (!StringUtility.isNullOrEmpty(getReturnTypeSignature()) && !Signature.SIG_VOID.equals(getReturnTypeSignature())) {
        builder.append("\nreturn " + ScoutUtility.getDefaultValueOf(getReturnTypeSignature()) + ";");
      }
    }
    return builder.toString();
  }

  public IMethod getCreatedMethod() {
    return m_createdMethod;
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public void setSibling(IJavaElement sibling) {
    m_sibling = sibling;
  }

  public IJavaElement getSibling() {
    return m_sibling;
  }

  public void setMethodFlags(int methodFlags) {
    m_methodFlags = methodFlags;
  }

  public int getMethodFlags() {
    return m_methodFlags;
  }

  public String getReturnTypeSignature() {
    return m_returnTypeSignature;
  }

  public void setReturnTypeSignature(String returnTypeSignature) {
    m_returnTypeSignature = returnTypeSignature;
  }

  public String getMethodName() {
    return m_methodName;
  }

  public void setSimpleBody(String simpleBody) {
    m_simpleBody = simpleBody;
  }

  public String getSimpleBody() {
    return m_simpleBody;
  }

  public void addExceptionSignature(String exceptionSignature) {
    m_exceptionSignatures.add(exceptionSignature);
  }

  public boolean removeExceptionSignature(String exceptionSignature) {
    return m_exceptionSignatures.remove(exceptionSignature);
  }

  public void setExceptionSignatures(String[] exceptionSignatures) {
    m_exceptionSignatures.clear();
    m_exceptionSignatures.addAll(Arrays.asList(exceptionSignatures));
  }

  public String[] getExceptionSignatures() {
    return m_exceptionSignatures.toArray(new String[m_exceptionSignatures.size()]);
  }

  public void setParameterSignatures(String[] parameterSignatures) {
    m_parameterSignatures = parameterSignatures;
  }

  public String[] getParameterSignatures() {
    return m_parameterSignatures;
  }

  public void setParameterNames(String[] parameterNames) {
    m_parameterNames = parameterNames;
  }

  public String[] getParameterNames() {
    return m_parameterNames;
  }

  public void addAnnotation(AnnotationCreateOperation annotation) {
    m_annotations.add(annotation);
  }

  public AnnotationCreateOperation[] getAnnotations() {
    return m_annotations.toArray(new AnnotationCreateOperation[m_annotations.size()]);
  }

}
