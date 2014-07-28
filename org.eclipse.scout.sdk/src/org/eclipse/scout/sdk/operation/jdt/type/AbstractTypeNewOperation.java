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
package org.eclipse.scout.sdk.operation.jdt.type;

import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.sourcebuilder.ICommentSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.annotation.IAnnotationSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;

/**
 * <h3>{@link AbstractTypeNewOperation}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 06.12.2012
 */
public abstract class AbstractTypeNewOperation implements ITypeNewOperation {

  private final ITypeSourceBuilder m_sourceBuilder;
  private boolean m_formatSource;

  // out members
  private IType m_createdType;

  protected AbstractTypeNewOperation(ITypeSourceBuilder sourceBuilder) {
    m_sourceBuilder = sourceBuilder;
  }

  @Override
  public String getOperationName() {
    return "create type '" + getSourceBuilder().getElementName() + "'...";
  }

  @Override
  public void validate() {
    getSourceBuilder().validate();
  }

  @Override
  public ITypeSourceBuilder getSourceBuilder() {
    return m_sourceBuilder;
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.AbstractJavaElementSourceBuilder#getElementName()
   */
  @Override
  public String getElementName() {
    return m_sourceBuilder.getElementName();
  }

  /**
   * @param commentSourceBuilder
   * @see org.eclipse.scout.sdk.sourcebuilder.AbstractJavaElementSourceBuilder#setCommentSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.ICommentSourceBuilder)
   */
  @Override
  public void setTypeCommentSourceBuilder(ICommentSourceBuilder commentSourceBuilder) {
    m_sourceBuilder.setCommentSourceBuilder(commentSourceBuilder);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.AbstractJavaElementSourceBuilder#getCommentSourceBuilder()
   */
  @Override
  public ICommentSourceBuilder getTypeCommentSourceBuilder() {
    return m_sourceBuilder.getCommentSourceBuilder();
  }

  /**
   * @param flags
   * @see org.eclipse.scout.sdk.sourcebuilder.AbstractAnnotatableSourceBuilder#setFlags(int)
   */
  @Override
  public void setFlags(int flags) {
    m_sourceBuilder.setFlags(flags);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.AbstractAnnotatableSourceBuilder#getFlags()
   */
  @Override
  public int getFlags() {
    return m_sourceBuilder.getFlags();
  }

  /**
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.AbstractAnnotatableSourceBuilder#addAnnotationSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.annotation.IAnnotationSourceBuilder)
   */
  @Override
  public void addAnnotationSourceBuilder(IAnnotationSourceBuilder builder) {
    m_sourceBuilder.addAnnotationSourceBuilder(builder);
  }

  /**
   * @param sortKey
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.AbstractAnnotatableSourceBuilder#addSortedAnnotationSourceBuilder(org.eclipse.scout.commons.CompositeObject,
   *      org.eclipse.scout.sdk.sourcebuilder.annotation.IAnnotationSourceBuilder)
   */
  @Override
  public void addSortedAnnotationSourceBuilder(CompositeObject sortKey, IAnnotationSourceBuilder builder) {
    m_sourceBuilder.addSortedAnnotationSourceBuilder(sortKey, builder);
  }

  /**
   * @param childOp
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.AbstractAnnotatableSourceBuilder#removeAnnotationSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.annotation.IAnnotationSourceBuilder)
   */
  @Override
  public boolean removeAnnotationSourceBuilder(IAnnotationSourceBuilder childOp) {
    return m_sourceBuilder.removeAnnotationSourceBuilder(childOp);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.AbstractAnnotatableSourceBuilder#getAnnotationSourceBuilders()
   */
  @Override
  public List<IAnnotationSourceBuilder> getAnnotationSourceBuilders() {
    return m_sourceBuilder.getAnnotationSourceBuilders();
  }

  /**
   * @param superTypeSignature
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#setSuperTypeSignature(java.lang.String)
   */
  @Override
  public void setSuperTypeSignature(String superTypeSignature) {
    m_sourceBuilder.setSuperTypeSignature(superTypeSignature);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#getSuperTypeSignature()
   */
  @Override
  public String getSuperTypeSignature() {
    return m_sourceBuilder.getSuperTypeSignature();
  }

  /**
   * @param interfaceSignature
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#addInterfaceSignature(java.lang.String)
   */
  @Override
  public void addInterfaceSignature(String interfaceSignature) {
    m_sourceBuilder.addInterfaceSignature(interfaceSignature);
  }

  /**
   * @param interfaceSignature
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#removeInterfaceSignature(java.lang.String)
   */
  @Override
  public boolean removeInterfaceSignature(String interfaceSignature) {
    return m_sourceBuilder.removeInterfaceSignature(interfaceSignature);
  }

  /**
   * @param interfaceSignatures
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#setInterfaceSignatures(java.lang.String[])
   */
  @Override
  public void setInterfaceSignatures(String[] interfaceSignatures) {
    m_sourceBuilder.setInterfaceSignatures(interfaceSignatures);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#getInterfaceSignatures()
   */
  @Override
  public List<String> getInterfaceSignatures() {
    return m_sourceBuilder.getInterfaceSignatures();
  }

  /**
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#addFieldSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder)
   */
  @Override
  public void addFieldSourceBuilder(IFieldSourceBuilder builder) {
    m_sourceBuilder.addFieldSourceBuilder(builder);
  }

  /**
   * @param sortKey
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#addSortedFieldSourceBuilder(org.eclipse.scout.commons.CompositeObject,
   *      org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder)
   */
  @Override
  public void addSortedFieldSourceBuilder(CompositeObject sortKey, IFieldSourceBuilder builder) {
    m_sourceBuilder.addSortedFieldSourceBuilder(sortKey, builder);
  }

  /**
   * @param builder
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#removeFieldSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.field.IFieldSourceBuilder)
   */
  @Override
  public boolean removeFieldSourceBuilder(IFieldSourceBuilder builder) {
    return m_sourceBuilder.removeFieldSourceBuilder(builder);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#getFieldSourceBuilders()
   */
  @Override
  public List<IFieldSourceBuilder> getFieldSourceBuilders() {
    return m_sourceBuilder.getFieldSourceBuilders();
  }

  /**
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#addMethodSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder)
   */
  @Override
  public void addMethodSourceBuilder(IMethodSourceBuilder builder) {
    m_sourceBuilder.addMethodSourceBuilder(builder);
  }

  /**
   * @param sortKey
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#addSortedMethodSourceBuilder(org.eclipse.scout.commons.CompositeObject,
   *      org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder)
   */
  @Override
  public void addSortedMethodSourceBuilder(CompositeObject sortKey, IMethodSourceBuilder builder) {
    m_sourceBuilder.addSortedMethodSourceBuilder(sortKey, builder);
  }

  /**
   * @param builder
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#removeMethodSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder)
   */
  @Override
  public boolean removeMethodSourceBuilder(IMethodSourceBuilder builder) {
    return m_sourceBuilder.removeMethodSourceBuilder(builder);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#getMethodSourceBuilders()
   */
  @Override
  public List<IMethodSourceBuilder> getMethodSourceBuilders() {
    return m_sourceBuilder.getMethodSourceBuilders();
  }

  /**
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#addTypeSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder)
   */
  @Override
  public void addTypeSourceBuilder(ITypeSourceBuilder builder) {
    m_sourceBuilder.addTypeSourceBuilder(builder);
  }

  /**
   * @param sortKey
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#addSortedTypeSourceBuilder(org.eclipse.scout.commons.CompositeObject,
   *      org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder)
   */
  @Override
  public void addSortedTypeSourceBuilder(CompositeObject sortKey, ITypeSourceBuilder builder) {
    m_sourceBuilder.addSortedTypeSourceBuilder(sortKey, builder);
  }

  /**
   * @param builder
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#removeTypeSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder)
   */
  @Override
  public boolean removeTypeSourceBuilder(ITypeSourceBuilder builder) {
    return m_sourceBuilder.removeTypeSourceBuilder(builder);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder#getTypeSourceBuilder()
   */
  @Override
  public List<ITypeSourceBuilder> getTypeSourceBuilder() {
    return m_sourceBuilder.getTypeSourceBuilder();
  }

  @Override
  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  @Override
  public boolean isFormatSource() {
    return m_formatSource;
  }

  protected void setCreatedType(IType createdType) {
    m_createdType = createdType;
  }

  @Override
  public IType getCreatedType() {
    return m_createdType;
  }
}
