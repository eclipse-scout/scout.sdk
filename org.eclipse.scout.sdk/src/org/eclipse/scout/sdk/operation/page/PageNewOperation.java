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
import org.eclipse.jdt.core.Signature;
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
import org.eclipse.scout.sdk.util.signature.SignatureUtility;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3> {@link PageNewOperation}</h3> ...
 */
public class PageNewOperation extends AbstractPageOperation {

  final IType iPageWithTable = TypeUtility.getType(RuntimeClasses.IPageWithTable);

  private final String m_typeName;
  private final String m_packageName;
  private final IJavaProject m_javaProject;
  private String m_superTypeSignature;
  private INlsEntry m_nlsEntry;
  private IType m_createdPage;
  private boolean m_formatSource;

  public PageNewOperation(String pageName, String packageName, IJavaProject javaProject) {
    m_typeName = pageName;
    m_packageName = packageName;
    m_javaProject = javaProject;
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
    if (getJavaProject() == null) {
      throw new IllegalArgumentException("client bundle can not be null.");
    }
    if (getTypeName() == null) {
      throw new IllegalArgumentException("type name can not be null.");
    }
    if (StringUtility.isNullOrEmpty(getPackageName())) {
      throw new IllegalArgumentException("package can not be null or empty.");
    }
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    PrimaryTypeNewOperation newOp = new PrimaryTypeNewOperation(getTypeName(), getPackageName(), getJavaProject());
    newOp.setIcuCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesCompilationUnitCommentBuilder());
    newOp.setTypeCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesTypeCommentBuilder());
    newOp.setFlags(Flags.AccPublic);
    newOp.setSuperTypeSignature(getSuperTypeSignature());
    // nls method
    if (getNlsEntry() != null) {
      IMethodSourceBuilder nlsTextMethodBuilder = MethodSourceBuilderFactory.createOverrideMethodSourceBuilder(newOp.getSourceBuilder(), SdkProperties.METHOD_NAME_GET_CONFIGURED_TITLE);
      nlsTextMethodBuilder.setMethodBodySourceBuilder(MethodBodySourceBuilderFactory.createNlsEntryReferenceBody(getNlsEntry()));
      newOp.addSortedMethodSourceBuilder(SortedMemberKeyFactory.createMethodGetConfiguredKey(nlsTextMethodBuilder), nlsTextMethodBuilder);
    }
    // table inner type
    String superTypeFqn = SignatureUtility.getFullyQuallifiedName(getSuperTypeSignature());
    if (CompareUtility.equals(superTypeFqn, RuntimeClasses.AbstractPageWithTable) || CompareUtility.equals(superTypeFqn, RuntimeClasses.AbstractExtensiblePageWithTable)) {
      ITypeSourceBuilder tableSourceBuilder = new TypeSourceBuilder(SdkProperties.TYPE_NAME_OUTLINE_WITH_TABLE_TABLE);
      tableSourceBuilder.setFlags(Flags.AccPublic);
      tableSourceBuilder.setSuperTypeSignature(RuntimeClasses.getSuperTypeSignature(RuntimeClasses.ITable, getJavaProject()));
      tableSourceBuilder.addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(10));
      newOp.addSortedTypeSourceBuilder(SortedMemberKeyFactory.createTypeTableKey(tableSourceBuilder), tableSourceBuilder);
      // update generic in supertype signature
      StringBuilder superTypeSigBuilder = new StringBuilder(superTypeFqn);
      superTypeSigBuilder.append("<").append(newOp.getPackageName()).append(".").append(newOp.getElementName()).append(".").append(SdkProperties.TYPE_NAME_OUTLINE_WITH_TABLE_TABLE).append(">");
      newOp.setSuperTypeSignature(Signature.createTypeSignature(superTypeSigBuilder.toString(), true));
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

  public String getPackageName() {
    return m_packageName;
  }

  public IJavaProject getJavaProject() {
    return m_javaProject;
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

}
