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
package org.eclipse.scout.sdk.operation.jdt.icu;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.Document;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.util.SourceFormatOperation;
import org.eclipse.scout.sdk.sourcebuilder.ICommentSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.SimpleImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link CompilationUnitUpdateOperation}</h3> ...
 * 
 * @author aho
 * @since 3.8.0 28.01.2013
 */
public class CompilationUnitUpdateOperation implements IOperation {

  private ICompilationUnit m_compilationUnit;
  private ICompilationUnitSourceBuilder m_sourceBuilder;
  private boolean m_formatSource;

  public CompilationUnitUpdateOperation(ICompilationUnit icu) {
    m_compilationUnit = icu;
    m_sourceBuilder = new CompilationUnitSourceBuilder(icu.getElementName(), icu.getParent().getElementName());
  }

  @Override
  public String getOperationName() {
    return "Update compilation unit '" + getCompilationUnit().getElementName() + "'.";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (!TypeUtility.exists(getCompilationUnit())) {
      throw new IllegalArgumentException("compilation unit to update can not be null!");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    ICompilationUnit icu = getCompilationUnit();
    workingCopyManager.register(icu, monitor);
    SimpleImportValidator validator = new SimpleImportValidator(TypeUtility.getPackage(icu).getElementName());
    StringBuilder source = new StringBuilder();
    getSourceBuilder().createSource(source, ResourceUtility.getLineSeparator(getCompilationUnit()), getCompilationUnit().getJavaProject(), validator);

    if (isFormatSource()) {
      Document icuDoc = new Document(source.toString());
      SourceFormatOperation sourceFormatOp = new SourceFormatOperation(getCompilationUnit().getJavaProject(), icuDoc, null);
      sourceFormatOp.run(monitor, workingCopyManager);
      // write document back
      source.replace(0, source.length(), icuDoc.get());
    }
    IBuffer buffer = icu.getBuffer();
    buffer.setContents(source.toString());
    buffer.save(monitor, true);
    workingCopyManager.reconcile(icu, monitor);
  }

  public ICompilationUnitSourceBuilder getSourceBuilder() {
    return m_sourceBuilder;
  }

  public ICompilationUnit getCompilationUnit() {
    return m_compilationUnit;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.IJavaElementSourceBuilder#getCommentSourceBuilder()
   */
  public ICommentSourceBuilder getCommentSourceBuilder() {
    return m_sourceBuilder.getCommentSourceBuilder();
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.IJavaElementSourceBuilder#getElementName()
   */
  public String getElementName() {
    return m_sourceBuilder.getElementName();
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder#getPackageFragmentName()
   */
  public String getPackageFragmentName() {
    return m_sourceBuilder.getPackageFragmentName();
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder#getTypeSourceBuilder()
   */
  public List<ITypeSourceBuilder> getTypeSourceBuilder() {
    return m_sourceBuilder.getTypeSourceBuilder();
  }

  /**
   * @param commentSourceBuilder
   * @see org.eclipse.scout.sdk.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder#setCommentSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.ICommentSourceBuilder)
   */
  public void setCommentSourceBuilder(ICommentSourceBuilder commentSourceBuilder) {
    m_sourceBuilder.setCommentSourceBuilder(commentSourceBuilder);
  }

  /**
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder#addTypeSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder)
   */
  public void addTypeSourceBuilder(ITypeSourceBuilder builder) {
    m_sourceBuilder.addTypeSourceBuilder(builder);
  }

  /**
   * @param sortKey
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder#addSortedTypeSourceBuilder(org.eclipse.scout.commons.CompositeObject,
   *      org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder)
   */
  public void addSortedTypeSourceBuilder(CompositeObject sortKey, ITypeSourceBuilder builder) {
    m_sourceBuilder.addSortedTypeSourceBuilder(sortKey, builder);
  }

  /**
   * @param builder
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder#removeTypeSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder)
   */
  public boolean removeTypeSourceBuilder(ITypeSourceBuilder builder) {
    return m_sourceBuilder.removeTypeSourceBuilder(builder);
  }

}
