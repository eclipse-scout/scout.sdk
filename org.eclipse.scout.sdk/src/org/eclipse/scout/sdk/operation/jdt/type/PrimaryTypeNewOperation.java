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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.sdk.operation.jdt.JavaElementFormatOperation;
import org.eclipse.scout.sdk.operation.jdt.icu.CompilationUnitNewOperation;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.sourcebuilder.ICommentSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link PrimaryTypeNewOperation}</h3> ...
 * 
 *  @author Andreas Hoegger
 * @since 3.10.0 08.03.2013
 */
public class PrimaryTypeNewOperation extends AbstractTypeNewOperation {

  private final CompilationUnitNewOperation m_compilationUnitNewOp;

  public PrimaryTypeNewOperation(String typeName, IPackageFragment declaringPackageFragment) throws JavaModelException {
    this(new TypeSourceBuilder(typeName), new CompilationUnitNewOperation(typeName + ".java", declaringPackageFragment.getElementName(), declaringPackageFragment.getJavaProject()));
  }

  public PrimaryTypeNewOperation(ITypeSourceBuilder sourceBuilder, IPackageFragment declaringPackageFragment) throws JavaModelException {
    this(sourceBuilder, new CompilationUnitNewOperation(sourceBuilder.getElementName() + ".java", declaringPackageFragment));
  }

  public PrimaryTypeNewOperation(String typeName, String packageName, IJavaProject project) throws JavaModelException {
    this(new TypeSourceBuilder(typeName), packageName, project);
  }

  public PrimaryTypeNewOperation(ITypeSourceBuilder sourceBuilder, String packageName, IJavaProject project) throws JavaModelException {
    this(sourceBuilder, new CompilationUnitNewOperation(sourceBuilder.getElementName() + ".java", packageName, project));
    sourceBuilder.setParentFullyQualifiedName(packageName);
  }

  private PrimaryTypeNewOperation(ITypeSourceBuilder sourceBuilder, CompilationUnitNewOperation icuOp) {
    super(sourceBuilder);
    m_compilationUnitNewOp = icuOp;
    sourceBuilder.setParentFullyQualifiedName(icuOp.getPackageFragmentName());
  }

  @Override
  public void validate() throws IllegalArgumentException {
    getCompilationUnitNewOp().validate();
    super.validate();
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    // create ICU
    CompilationUnitNewOperation icuNewOp = getCompilationUnitNewOp();
    icuNewOp.addTypeSourceBuilder(getSourceBuilder());
    icuNewOp.validate();
    icuNewOp.run(monitor, workingCopyManager);
    setCreatedType(getCreatedCompilationUnit().getType(getElementName()));
    if (isFormatSource()) {
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedCompilationUnit(), true);
      formatOp.validate();
      formatOp.run(monitor, workingCopyManager);
    }
  }

  protected CompilationUnitNewOperation getCompilationUnitNewOp() {
    return m_compilationUnitNewOp;
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.operation.jdt.icu.CompilationUnitNewOperation#getJavaProject()
   */
  public IJavaProject getJavaProject() {
    return getCompilationUnitNewOp().getJavaProject();
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.operation.jdt.icu.CompilationUnitNewOperation#getPackageFragmentName()
   */

  public String getPackageName() {
    return getCompilationUnitNewOp().getPackageFragmentName();
  }

  /**
   * @param packageExportPolicy
   * @see org.eclipse.scout.sdk.operation.jdt.icu.CompilationUnitNewOperation#setPackageExportPolicy(org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy)
   */
  public void setPackageExportPolicy(ExportPolicy packageExportPolicy) {
    m_compilationUnitNewOp.setPackageExportPolicy(packageExportPolicy);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.operation.jdt.icu.CompilationUnitNewOperation#getPackageExportPolicy()
   */
  public ExportPolicy getPackageExportPolicy() {
    return m_compilationUnitNewOp.getPackageExportPolicy();
  }

  /**
   * @param formatSource
   * @see org.eclipse.scout.sdk.operation.jdt.icu.CompilationUnitNewOperation#setFormatSource(boolean)
   */
  @Override
  public void setFormatSource(boolean formatSource) {
    m_compilationUnitNewOp.setFormatSource(formatSource);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.operation.jdt.icu.CompilationUnitNewOperation#isFormatSource()
   */
  @Override
  public boolean isFormatSource() {
    return m_compilationUnitNewOp.isFormatSource();
  }

  /**
   * @param commentSourceBuilder
   * @see org.eclipse.scout.sdk.operation.jdt.icu.CompilationUnitNewOperation#setCommentSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.ICommentSourceBuilder)
   */
  public void setIcuCommentSourceBuilder(ICommentSourceBuilder commentSourceBuilder) {
    m_compilationUnitNewOp.setCommentSourceBuilder(commentSourceBuilder);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.operation.jdt.icu.CompilationUnitNewOperation#getCommentSourceBuilder()
   */
  public ICommentSourceBuilder getIcuCommentSourceBuilder() {
    return m_compilationUnitNewOp.getCommentSourceBuilder();
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.operation.jdt.icu.CompilationUnitNewOperation#getCreatedCompilationUnit()
   */
  public ICompilationUnit getCreatedCompilationUnit() {
    return m_compilationUnitNewOp.getCreatedCompilationUnit();
  }

}
