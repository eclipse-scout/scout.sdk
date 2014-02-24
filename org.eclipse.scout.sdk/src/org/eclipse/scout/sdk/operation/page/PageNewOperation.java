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
package org.eclipse.scout.sdk.operation.page;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.nls.sdk.model.INlsEntry;
import org.eclipse.scout.sdk.extensions.runtime.classes.RuntimeClasses;
import org.eclipse.scout.sdk.operation.jdt.packageFragment.ExportPolicy;
import org.eclipse.scout.sdk.operation.jdt.type.PrimaryTypeNewOperation;
import org.eclipse.scout.sdk.sourcebuilder.SortedMemberKeyFactory;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.IMethodSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodBodySourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.method.MethodSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.SdkProperties;
import org.eclipse.scout.sdk.util.internal.sigcache.SignatureCache;
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3> {@link PageNewOperation}</h3> Use to create a new Page.
 */
public class PageNewOperation extends AbstractPageOperation {

  private final String m_typeName;
  private final String m_packageNamePage;
  private final String m_packageNamePageData;
  private final IJavaProject m_pageJavaProject;
  private final IJavaProject m_pageDataJavaProject;
  private String m_superTypeSignature;
  private INlsEntry m_nlsEntry;
  private boolean m_formatSource;

  private IType m_createdPage;
  private IType m_createdPageData;

  public PageNewOperation(String pageName, String packageNamePage, String packageNamePageData, IJavaProject pageJavaProject, IJavaProject pageDataJavaProject) {
    m_typeName = pageName;
    m_packageNamePage = packageNamePage;
    m_packageNamePageData = packageNamePageData;
    m_pageJavaProject = pageJavaProject;
    m_pageDataJavaProject = pageDataJavaProject;
  }

  @Override
  public String getOperationName() {
    return "New page '" + getTypeName() + "'...";
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (StringUtility.isNullOrEmpty(getSuperTypeSignature())) {
      throw new IllegalArgumentException("super type can not be null.");
    }
    if (getPageJavaProject() == null) {
      throw new IllegalArgumentException("client bundle can not be null.");
    }
    if (getTypeName() == null) {
      throw new IllegalArgumentException("type name can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getPackageNamePage())) {
      throw new IllegalArgumentException("package can not be null or empty.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    String superTypeFqn = SignatureUtility.getFullyQualifiedName(getSuperTypeSignature());

    PrimaryTypeNewOperation newOp = new PrimaryTypeNewOperation(getTypeName(), getPackageNamePage(), getPageJavaProject());
    newOp.setIcuCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesCompilationUnitCommentBuilder());
    newOp.setTypeCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesTypeCommentBuilder());
    newOp.setFlags(Flags.AccPublic);
    newOp.setSuperTypeSignature(getSuperTypeSignature());

    // page data
    ITypeHierarchy pageSuperTypeHierarchy = TypeUtility.getSuperTypeHierarchy(TypeUtility.getType(superTypeFqn));
    if (getPageDataJavaProject() != null && pageSuperTypeHierarchy.contains(TypeUtility.getType(RuntimeClasses.IPageWithTable))) {
      String pageDataTypeName = getTypeName() + "Data";
      PrimaryTypeNewOperation pageDataTypeNewOp = new PrimaryTypeNewOperation(pageDataTypeName, getPackageNamePageData(), getPageDataJavaProject());
      pageDataTypeNewOp.addMethodSourceBuilder(MethodSourceBuilderFactory.createConstructorSourceBuilder(pageDataTypeName));
      pageDataTypeNewOp.setFlags(Flags.AccPublic);
      pageDataTypeNewOp.setSuperTypeSignature(SignatureCache.createTypeSignature(RuntimeClasses.AbstractTablePageData));
      pageDataTypeNewOp.setPackageExportPolicy(ExportPolicy.AddPackage);
      pageDataTypeNewOp.setFormatSource(false);
      pageDataTypeNewOp.validate();
      pageDataTypeNewOp.run(monitor, workingCopyManager);
      m_createdPageData = pageDataTypeNewOp.getCreatedType();
      workingCopyManager.register(m_createdPageData.getCompilationUnit(), monitor);

      // page data annotation
      String pageDataSig = SignatureCache.createTypeSignature(m_createdPageData.getFullyQualifiedName());
      newOp.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createPageDataAnnotation(pageDataSig));
    }

    // nls method
    if (getNlsEntry() != null) {
      IMethodSourceBuilder nlsTextMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(newOp.getSourceBuilder(), SdkProperties.METHOD_NAME_GET_CONFIGURED_TITLE);
      nlsTextMethodBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(getNlsEntry()));
      newOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(nlsTextMethodBuilder), nlsTextMethodBuilder);
    }

    // table inner type
    if (CompareUtility.equals(superTypeFqn, RuntimeClasses.AbstractPageWithTable) || CompareUtility.equals(superTypeFqn, RuntimeClasses.AbstractExtensiblePageWithTable)) {
      ITypeSourceBuilder tableSourceBuilder = new TypeSourceBuilder(SdkProperties.TYPE_NAME_OUTLINE_WITH_TABLE_TABLE);
      tableSourceBuilder.setFlags(Flags.AccPublic);
      tableSourceBuilder.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.ITable, getPageJavaProject()));
      tableSourceBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(10));
      newOp.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeTableKey(tableSourceBuilder), tableSourceBuilder);

      // update generic in supertype signature
      StringBuilder superTypeSigBuilder = new StringBuilder(superTypeFqn);
      superTypeSigBuilder.append("<").append(newOp.getPackageName()).append(".").append(newOp.getElementName()).append(".").append(SdkProperties.TYPE_NAME_OUTLINE_WITH_TABLE_TABLE).append(">");
      newOp.setSuperTypeSignature(SignatureCache.createTypeSignature(superTypeSigBuilder.toString()));
    }

    newOp.setPackageExportPolicy(ExportPolicy.AddPackage);
    newOp.setFormatSource(isFormatSource());

    newOp.validate();
    newOp.run(monitor, workingCopyManager);

    m_createdPage = newOp.getCreatedType();

    workingCopyManager.register(getCreatedPage().getCompilationUnit(), monitor);
    addToHolder(getCreatedPage(), monitor, workingCopyManager);
  }

  public IType getCreatedPage() {
    return m_createdPage;
  }

  public String getTypeName() {
    return m_typeName;
  }

  public String getPackageNamePage() {
    return m_packageNamePage;
  }

  public IJavaProject getPageJavaProject() {
    return m_pageJavaProject;
  }

  public void setSuperTypeSignature(String superTypeSignature) {
    m_superTypeSignature = superTypeSignature;
  }

  public String getSuperTypeSignature() {
    return m_superTypeSignature;
  }

  public INlsEntry getNlsEntry() {
    return m_nlsEntry;
  }

  public void setNlsEntry(INlsEntry nlsEntry) {
    m_nlsEntry = nlsEntry;
  }

  public boolean isFormatSource() {
    return m_formatSource;
  }

  public void setFormatSource(boolean formatSource) {
    m_formatSource = formatSource;
  }

  public IType getCreatedPageData() {
    return m_createdPageData;
  }

  public IJavaProject getPageDataJavaProject() {
    return m_pageDataJavaProject;
  }

  public String getPackageNamePageData() {
    return m_packageNamePageData;
  }
}
