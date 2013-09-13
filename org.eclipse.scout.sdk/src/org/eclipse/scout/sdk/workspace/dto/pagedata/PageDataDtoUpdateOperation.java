/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.sdk.workspace.dto.pagedata;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.internal.workspace.dto.FormDataUtility;
import org.eclipse.scout.sdk.operation.jdt.icu.CompilationUnitUpdateOperation;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.SimpleImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.workspace.dto.AbstractDtoAutoUpdateOperation;

/**
 * <h3>{@link PageDataDtoUpdateOperation}</h3>
 * 
 * @author aho
 * @since 3.10.0 16.08.2013
 */
public class PageDataDtoUpdateOperation extends AbstractDtoAutoUpdateOperation {

  private final PageDataAnnotation m_pageDataAnnotation;
  private ITypeHierarchy m_superTypeHierarchy;

  /**
   * @param modelType
   */
  public PageDataDtoUpdateOperation(IType modelType, PageDataAnnotation pageDataAnnotation, ITypeHierarchy superTypeHierarchy) {
    super(modelType);
    m_pageDataAnnotation = pageDataAnnotation;
    m_superTypeHierarchy = superTypeHierarchy;
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (getPageDataAnnotation() == null) {
      throw new IllegalArgumentException("FormDataAnnotation can not be null.");
    }
    super.validate();
  }

  public PageDataAnnotation getPageDataAnnotation() {
    return m_pageDataAnnotation;
  }

  @Override
  protected String getDerivedTypeSignature() throws CoreException {
    PageDataAnnotation pageDataAnnotation = getPageDataAnnotation();
    if (pageDataAnnotation != null) {
      return pageDataAnnotation.getPageDataTypeSignature();
    }
    return null;
  }

  @Override
  protected String createDerivedTypeSource(IProgressMonitor monitor) throws CoreException {
    PageDataAnnotation pageDataAnnotation = getPageDataAnnotation();
    ITypeSourceBuilder pageDataSourceBuilder = FormDataUtility.createPageDataSourceBuilder(getModelType(), pageDataAnnotation);

    IType dtoType = ensureDerivedType();
    if (!TypeUtility.exists(dtoType)) {
      return null;
    }
    ICompilationUnit dtoIcu = dtoType.getCompilationUnit();

    CompilationUnitUpdateOperation icuUpdateOp = new CompilationUnitUpdateOperation(dtoIcu);
    icuUpdateOp.addTypeSourceBuilder(pageDataSourceBuilder);

    SimpleImportValidator validator = new SimpleImportValidator(TypeUtility.getPackage(dtoIcu).getElementName());
    StringBuilder sourceBuilder = new StringBuilder();
    icuUpdateOp.getSourceBuilder().createSource(sourceBuilder, ResourceUtility.getLineSeparator(dtoIcu), dtoIcu.getJavaProject(), validator);
    String source = sourceBuilder.toString();
    return source;
  }

}
