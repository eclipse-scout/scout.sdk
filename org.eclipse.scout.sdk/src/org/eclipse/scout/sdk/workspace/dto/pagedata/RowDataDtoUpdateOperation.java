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
import org.eclipse.scout.sdk.internal.workspace.dto.DtoUtility;
import org.eclipse.scout.sdk.sourcebuilder.comment.CommentSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.compilationunit.CompilationUnitSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.ImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.workspace.dto.AbstractDtoAutoUpdateOperation;

/**
 * <h3>{@link RowDataDtoUpdateOperation}</h3>
 *
 * @author Matthias Villiger
 * @since 4.1.0 19.11.2014
 */
public class RowDataDtoUpdateOperation extends AbstractDtoAutoUpdateOperation {

  private final DataAnnotation m_dataAnnotation;

  /**
   * @param modelType
   */
  public RowDataDtoUpdateOperation(IType modelType, DataAnnotation dataAnnotation) {
    super(modelType);
    m_dataAnnotation = dataAnnotation;
  }

  @Override
  public void validate() {
    if (getDataAnnotation() == null) {
      throw new IllegalArgumentException("FormDataAnnotation can not be null.");
    }
    super.validate();
  }

  public DataAnnotation getDataAnnotation() {
    return m_dataAnnotation;
  }

  @Override
  protected String getDerivedTypeSignature() throws CoreException {
    DataAnnotation dataAnnotation = getDataAnnotation();
    if (dataAnnotation != null) {
      return dataAnnotation.getDataTypeSignature();
    }
    return null;
  }

  @Override
  protected String createDerivedTypeSource(IProgressMonitor monitor) throws CoreException {
    IType dtoType = ensureDerivedType();
    if (!TypeUtility.exists(dtoType)) {
      return null;
    }
    ICompilationUnit dtoIcu = dtoType.getCompilationUnit();

    ITypeSourceBuilder rowDataSourceBuilder = DtoUtility.createTableRowDataTypeSourceBuilder(getModelType(), getDataAnnotation(), monitor);
    if (monitor.isCanceled()) {
      return null;
    }

    CompilationUnitSourceBuilder cuSourceBuilder = new CompilationUnitSourceBuilder(dtoIcu.getElementName(), dtoIcu.getParent().getElementName());
    cuSourceBuilder.addTypeSourceBuilder(rowDataSourceBuilder);
    cuSourceBuilder.setCommentSourceBuilder(CommentSourceBuilderFactory.createPreferencesCompilationUnitCommentBuilder());

    ImportValidator validator = new ImportValidator(TypeUtility.getPackage(dtoIcu).getElementName());
    if (monitor.isCanceled()) {
      return null;
    }

    StringBuilder sourceBuilder = new StringBuilder();
    cuSourceBuilder.createSource(sourceBuilder, ResourceUtility.getLineSeparator(dtoIcu), dtoIcu.getJavaProject(), validator);
    String source = sourceBuilder.toString();
    return source;
  }
}
