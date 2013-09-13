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
package org.eclipse.scout.sdk.operation.jdt.field;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.sourcebuilder.ICommentSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.CompilationUnitImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link FieldNewOperation}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 20.12.2012
 */
public class FieldNewOperation implements IOperation {

  private final IType m_declaringType;
  private IJavaElement m_sibling;

  private final IFieldSourceBuilder m_sourceBuilder;
  private boolean m_formatSource;

  private IField m_createdField;

  public FieldNewOperation(String fieldName, IType declaringType) {
    this(fieldName, declaringType, true);
  }

  public FieldNewOperation(String fieldName, IType declaringType, boolean formatSource) {
    this(new FieldSourceBuilder(fieldName), declaringType, formatSource);
  }

  public FieldNewOperation(IFieldSourceBuilder sourceBuilder, IType declaringType) {
    this(sourceBuilder, declaringType, true);
  }

  public FieldNewOperation(IFieldSourceBuilder sourceBuilder, IType declaringType, boolean formatSource) {
    m_sourceBuilder = sourceBuilder;
    m_declaringType = declaringType;
    m_formatSource = formatSource;
  }

  @Override
  public String getOperationName() {
    return "create field '" + getSourceBuilder().getElementName() + "'...";
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
    m_sourceBuilder.createSource(sourceBuilder, ResourceUtility.getLineSeparator(icu), getDeclaringType().getJavaProject(), importValidator);
    setCreatedField(getDeclaringType().createField(sourceBuilder.toString(), getSibling(), true, monitor));
    importValidator.createImports(monitor);

  }

  protected IFieldSourceBuilder getSourceBuilder() {
    return m_sourceBuilder;
  }

  public IType getDeclaringType() {
    return m_declaringType;
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
   * @param signature
   * @see org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilder#setSignature(java.lang.String)
   */
  public void setSignature(String signature) {
    m_sourceBuilder.setSignature(signature);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilder#getSignature()
   */
  public String getSignature() {
    return m_sourceBuilder.getSignature();
  }

  /**
   * @param value
   * @see org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilder#setValue(java.lang.String)
   */
  public void setValue(String value) {
    m_sourceBuilder.setValue(value);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.field.FieldSourceBuilder#getValue()
   */
  public String getValue() {
    return m_sourceBuilder.getValue();
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

  protected void setCreatedField(IField createdField) {
    m_createdField = createdField;
  }

  public IField getCreatedField() {
    return m_createdField;
  }

}
