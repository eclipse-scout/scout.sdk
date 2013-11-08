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
package org.eclipse.scout.sdk.operation.jdt.method;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.JavaElementFormatOperation;
import org.eclipse.scout.sdk.sourcebuilder.ICommentSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.CompilationUnitImportValidator;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.type.MethodParameter;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link AbstractMethodNewOperation}</h3> ...
 * 
 * @author Andreas Hoegger
 * @since 3.10.0 13.03.2013
 */
public abstract class AbstractMethodNewOperation implements IOperation {

  private final IType m_declaringType;
  private final IMethodSourceBuilder m_sourceBuilder;
  private boolean m_formatSource;
  private IJavaElement m_sibling;

  private IMethod m_createdMethod;

  public AbstractMethodNewOperation(String methodName, IType declaringType) {
    this(methodName, declaringType, true);
  }

  public AbstractMethodNewOperation(String methodName, IType declaringType, boolean formatSource) {
    this(new MethodSourceBuilder(methodName), declaringType, formatSource);
  }

  public AbstractMethodNewOperation(IMethodSourceBuilder sourceBuilder, IType declaringType) {
    this(sourceBuilder, declaringType, false);

  }

  public AbstractMethodNewOperation(IMethodSourceBuilder sourceBuilder, IType declaringType, boolean formatSource) {
    m_declaringType = declaringType;
    m_sourceBuilder = sourceBuilder;
    m_formatSource = formatSource;
  }

  @Override
  public String getOperationName() {
    return "create method '" + getSourceBuilder().getElementName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (!TypeUtility.exists(getDeclaringType())) {
      throw new IllegalArgumentException("declaring type does not exist!");
    }
    m_sourceBuilder.validate();
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    ICompilationUnit icu = getDeclaringType().getCompilationUnit();
    CompilationUnitImportValidator importValidator = new CompilationUnitImportValidator(icu);
    StringBuilder sourceBuilder = new StringBuilder();
    createSource(sourceBuilder, ResourceUtility.getLineSeparator(icu), getDeclaringType().getJavaProject(), importValidator);
    workingCopyManager.register(icu, monitor);

    setCreatedMethod(getDeclaringType().createMethod(sourceBuilder.toString(), getSibling(), true, monitor));
    importValidator.createImports(monitor);
    if (isFormatSource()) {
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedMethod(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  protected void createSource(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
    m_sourceBuilder.createSource(source, lineDelimiter, ownerProject, validator);
  }

  public IType getDeclaringType() {
    return m_declaringType;
  }

  public IMethodSourceBuilder getSourceBuilder() {
    return m_sourceBuilder;
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

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.AbstractJavaElementSourceBuilder#getElementName()
   *      TODO JDT change to getmethodname
   */
  public String getElementName() {
    return m_sourceBuilder.getElementName();
  }

  /**
   * @param commentSourceBuilder
   * @see org.eclipse.scout.sdk.sourcebuilder.AbstractJavaElementSourceBuilder#setCommentSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.ICommentSourceBuilder)
   */
  public void setCommentSourceBuilder(ICommentSourceBuilder commentSourceBuilder) {
    m_sourceBuilder.setCommentSourceBuilder(commentSourceBuilder);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.AbstractJavaElementSourceBuilder#getCommentSourceBuilder()
   */
  public ICommentSourceBuilder getCommentSourceBuilder() {
    return m_sourceBuilder.getCommentSourceBuilder();
  }

  /**
   * @param flags
   * @see org.eclipse.scout.sdk.sourcebuilder.AbstractAnnotatableSourceBuilder#setFlags(int)
   */
  public void setFlags(int flags) {
    m_sourceBuilder.setFlags(flags);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.AbstractAnnotatableSourceBuilder#getFlags()
   */
  public int getFlags() {
    return m_sourceBuilder.getFlags();
  }

  /**
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.AbstractAnnotatableSourceBuilder#addAnnotationSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.annotation.IAnnotationSourceBuilder)
   */
  public void addAnnotationSourceBuilder(IAnnotationSourceBuilder builder) {
    m_sourceBuilder.addAnnotationSourceBuilder(builder);
  }

  /**
   * @param sortKey
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.AbstractAnnotatableSourceBuilder#addSortedAnnotationSourceBuilder(org.eclipse.scout.commons.CompositeObject,
   *      org.eclipse.scout.sdk.sourcebuilder.annotation.IAnnotationSourceBuilder)
   */
  public void addSortedAnnotationSourceBuilder(CompositeObject sortKey, IAnnotationSourceBuilder builder) {
    m_sourceBuilder.addSortedAnnotationSourceBuilder(sortKey, builder);
  }

  /**
   * @param childOp
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.AbstractAnnotatableSourceBuilder#removeAnnotationSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.annotation.IAnnotationSourceBuilder)
   */
  public boolean removeAnnotationSourceBuilder(IAnnotationSourceBuilder childOp) {
    return m_sourceBuilder.removeAnnotationSourceBuilder(childOp);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.AbstractAnnotatableSourceBuilder#getAnnotationSourceBuilders()
   */
  public List<IAnnotationSourceBuilder> getAnnotationSourceBuilders() {
    return m_sourceBuilder.getAnnotationSourceBuilders();
  }

  /**
   * @param parameters
   * @see org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder#setParameters(org.eclipse.scout.sdk.util.type.MethodParameter[])
   */
  public void setParameters(MethodParameter[] parameters) {
    m_sourceBuilder.setParameters(parameters);
  }

  /**
   * @param parameter
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder#addParameter(org.eclipse.scout.sdk.util.type.MethodParameter)
   */
  public boolean addParameter(MethodParameter parameter) {
    return m_sourceBuilder.addParameter(parameter);
  }

  /**
   * @param parameter
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder#removeParameter(org.eclipse.scout.sdk.util.type.MethodParameter)
   */
  public boolean removeParameter(MethodParameter parameter) {
    return m_sourceBuilder.removeParameter(parameter);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder#getParameters()
   */
  public List<MethodParameter> getParameters() {
    return m_sourceBuilder.getParameters();
  }

  /**
   * @param exceptionSignature
   * @see org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder#addExceptionSignature(java.lang.String)
   */
  public void addExceptionSignature(String exceptionSignature) {
    m_sourceBuilder.addExceptionSignature(exceptionSignature);
  }

  /**
   * @param exceptionSignature
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder#removeExceptionSignature(java.lang.String)
   */
  public boolean removeExceptionSignature(String exceptionSignature) {
    return m_sourceBuilder.removeExceptionSignature(exceptionSignature);
  }

  /**
   * @param exceptionSignatures
   * @see org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder#setExceptionSignatures(java.lang.String[])
   */
  public void setExceptionSignatures(String[] exceptionSignatures) {
    m_sourceBuilder.setExceptionSignatures(exceptionSignatures);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder#getExceptionSignatures()
   */
  public List<String> getExceptionSignatures() {
    return m_sourceBuilder.getExceptionSignatures();
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder#getMethodBodySourceBuilder()
   */
  public IMethodBodySourceBuilder getMethodBodySourceBuilder() {
    return m_sourceBuilder.getMethodBodySourceBuilder();
  }

  /**
   * @param methodBodySourceBuilder
   * @see org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilder#setMethodBodySourceBuilder(org.eclipse.scout.sdk.sourcebuilder.method.IMethodBodySourceBuilder)
   */
  public void setMethodBodySourceBuilder(IMethodBodySourceBuilder methodBodySourceBuilder) {
    m_sourceBuilder.setMethodBodySourceBuilder(methodBodySourceBuilder);
  }

  /**
   * @param createMethod
   */
  protected void setCreatedMethod(IMethod createMethod) {
    m_createdMethod = createMethod;
  }

  public IMethod getCreatedMethod() {
    return m_createdMethod;
  }

}
