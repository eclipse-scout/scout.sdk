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
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.SimpleImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.dto.AbstractDtoAutoUpdateOperation;

/**
 * <h3>{@link PageDataDtoUpdateOperation}</h3>
 * 
 *  @author Andreas Hoegger
 * @since 3.10.0 16.08.2013
 */
public class PageDataDtoUpdateOperation extends AbstractDtoAutoUpdateOperation {

  private final PageDataAnnotation m_pageDataAnnotation;

  /**
   * @param modelType
   */
  public PageDataDtoUpdateOperation(IType modelType, PageDataAnnotation pageDataAnnotation) {
    super(modelType);
    m_pageDataAnnotation = pageDataAnnotation;
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

    CompilationUnitSourceBuilder cuSourceBuilder = new CompilationUnitSourceBuilder(dtoIcu.getElementName(), dtoIcu.getParent().getElementName());
    cuSourceBuilder.addTypeSourceBuilder(pageDataSourceBuilder);
    cuSourceBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesCompilationUnitCommentBuilder());

    SimpleImportValidator validator = new SimpleImportValidator(TypeUtility.getPackage(dtoIcu).getElementName());

    // loop through all types recursively to ensure all simple names that will be created are "consumed" in the import validator
    consumeAllTypeNamesRec(pageDataSourceBuilder, validator);

    StringBuilder sourceBuilder = new StringBuilder();
    cuSourceBuilder.createSource(sourceBuilder, ResourceUtility.getLineSeparator(dtoIcu), dtoIcu.getJavaProject(), validator);
    String source = sourceBuilder.toString();
    return source;
  }

}
