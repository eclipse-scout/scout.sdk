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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.scout.commons.CompositeObject;
import org.eclipse.scout.sdk.operation.IOperation;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.PackageFragementNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.ICommentSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.compilationunit.ICompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.IImportValidator;
import org.eclipse.scout.sdk.util.signature.ImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link CompilationUnitNewOperation}</h3>
 * 
 *  @author Andreas Hoegger
 * @since 3.10.0 08.01.2013
 */
public class CompilationUnitNewOperation implements IOperation {

  private final ICompilationUnitSourceBuilder m_sourceBuilder;
  private IPackageFragment m_packageFragment;
  private final IJavaProject m_javaProject;
  private boolean m_formatSource;
  private ExportPolicy m_packageExportPolicy;

  private ICompilationUnit m_createdCompilationUnit;

  public CompilationUnitNewOperation(String compilationUnitName, String packageName, IJavaProject javaProject) throws JavaModelException {
    this(new CompilationUnitSourceBuilder(compilationUnitName, packageName), javaProject);
  }

  public CompilationUnitNewOperation(String compilationUnitName, IPackageFragment packageFragment) throws JavaModelException {
    this(new CompilationUnitSourceBuilder(compilationUnitName, packageFragment.getElementName()), packageFragment.getJavaProject());
    m_packageFragment = packageFragment;
  }

  public CompilationUnitNewOperation(ICompilationUnitSourceBuilder sourceBuilder, IJavaProject javaProject) throws JavaModelException {
    m_sourceBuilder = sourceBuilder;
    m_javaProject = javaProject;
  }

  @Override
  public String getOperationName() {
    return "Create compilation unit '" + getSourceBuilder().getElementName() + "'...";
  }

  @Override
  public void validate() {
    getSourceBuilder().validate();
    if (!TypeUtility.exists(getJavaProject())) {
      throw new IllegalArgumentException("java project does not exist!");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (getPackageFragment() == null) {
      PackageFragementNewOperation packageOp = new PackageFragementNewOperation(getPackageFragmentName(), getJavaProject());
      packageOp.setNoErrorWhenPackageAlreadyExist(true);
      ExportPolicy packageExportPolicy = getPackageExportPolicy();
      if (ExportPolicy.ADD_PACKAGE_WHEN_NOT_EMPTY.equals(packageExportPolicy)) {
        packageExportPolicy = ExportPolicy.ADD_PACKAGE;
      }
      packageOp.setExportPackagePolicy(packageExportPolicy);
      packageOp.validate();
      packageOp.run(monitor, workingCopyManager);
      setPackageFragment(packageOp.getCreatedPackageFragment());
    }

    // create icu
    StringBuilder source = new StringBuilder();
    createSource(source, ResourceUtility.getLineSeparator(getPackageFragment()), getPackageFragment().getJavaProject(), new ImportValidator(getPackageFragment().getElementName()));

    ICompilationUnit createdCompilationUnit = getPackageFragment().createCompilationUnit(getSourceBuilder().getElementName(), source.toString(), true, monitor);
    workingCopyManager.register(createdCompilationUnit, monitor);
    setCreatedCompilationUnit(createdCompilationUnit);
  }

  protected void createSource(StringBuilder source, String lineDelimiter, IJavaProject ownerProject, IImportValidator validator) throws CoreException {
    getSourceBuilder().createSource(source, lineDelimiter, ownerProject, validator);
  }

  public IJavaProject getJavaProject() {
    return m_javaProject;
  }

  protected void setPackageFragment(IPackageFragment packageFragment) {
    m_packageFragment = packageFragment;
  }

  public IPackageFragment getPackageFragment() {
    return m_packageFragment;
  }

  public void setPackageExportPolicy(ExportPolicy packageExportPolicy) {
    m_packageExportPolicy = packageExportPolicy;
  }

  public ExportPolicy getPackageExportPolicy() {
    return m_packageExportPolicy;
  }

  protected ICompilationUnitSourceBuilder getSourceBuilder() {
    return m_sourceBuilder;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public boolean isFormatSource() {
    return m_formatSource;
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
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.compilationunit.CompilationUnitSourceBuilder#getPackageFragmentName()
   */
  public String getPackageFragmentName() {
    return m_sourceBuilder.getPackageFragmentName();
  }

  /**
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.compilationunit.CompilationUnitSourceBuilder#addTypeSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder)
   */
  public void addTypeSourceBuilder(ITypeSourceBuilder builder) {
    m_sourceBuilder.addTypeSourceBuilder(builder);
  }

  /**
   * @param sortKey
   * @param builder
   * @see org.eclipse.scout.sdk.sourcebuilder.compilationunit.CompilationUnitSourceBuilder#addSortedTypeSourceBuilder(org.eclipse.scout.commons.CompositeObject,
   *      org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder)
   */
  public void addSortedTypeSourceBuilder(CompositeObject sortKey, ITypeSourceBuilder builder) {
    m_sourceBuilder.addSortedTypeSourceBuilder(sortKey, builder);
  }

  /**
   * @param builder
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.compilationunit.CompilationUnitSourceBuilder#removeTypeSourceBuilder(org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder)
   */
  public boolean removeTypeSourceBuilder(ITypeSourceBuilder builder) {
    return m_sourceBuilder.removeTypeSourceBuilder(builder);
  }

  /**
   * @return
   * @see org.eclipse.scout.sdk.sourcebuilder.compilationunit.CompilationUnitSourceBuilder#getTypeSourceBuilder()
   */
  public List<ITypeSourceBuilder> getTypeSourceBuilder() {
    return m_sourceBuilder.getTypeSourceBuilder();
  }

  protected void setCreatedCompilationUnit(ICompilationUnit createdCompilationUnit) {
    m_createdCompilationUnit = createdCompilationUnit;
  }

  public ICompilationUnit getCreatedCompilationUnit() {
    return m_createdCompilationUnit;
  }

}
