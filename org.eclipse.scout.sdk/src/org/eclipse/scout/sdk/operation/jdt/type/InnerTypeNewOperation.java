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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.operation.jdt.JavaElementFormatOperation;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.CompilationUnitImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link InnerTypeNewOperation}</h3> ...
 * 
 * @author aho
 * @since 3.10.0 08.03.2013
 */
public class InnerTypeNewOperation extends AbstractTypeNewOperation {

  private IJavaElement m_sibling;
  private final IType m_declaringType;

  public InnerTypeNewOperation(String typeName, IType declaringType) {
    this(new TypeSourceBuilder(typeName), declaringType);
  }

  public InnerTypeNewOperation(ITypeSourceBuilder sourceBuilder, IType declaringType) {
    super(sourceBuilder);
    sourceBuilder.setParentFullyQualifiedName(declaringType.getFullyQualifiedName());
    m_declaringType = declaringType;
  }

  @Override
  public void validate() throws IllegalArgumentException {
    if (!TypeUtility.exists(getDeclaringType())) {
      throw new IllegalArgumentException("Declaring type does not exist!");
    }
    super.validate();
  }

  @Override
  public void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    ICompilationUnit icu = getDeclaringType().getCompilationUnit();
    CompilationUnitImportValidator importValidator = new CompilationUnitImportValidator(icu);
    StringBuilder sourceBuilder = new StringBuilder();
    getSourceBuilder().createSource(sourceBuilder, ResourceUtility.getLineSeparator(icu), getDeclaringType().getJavaProject(), importValidator);
    setCreatedType(getDeclaringType().createType(sourceBuilder.toString(), getSibling(), true, monitor));
    importValidator.createImports(monitor);
    if (isFormatSource()) {
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedType(), true);
      formatOp.run(monitor, workingCopyManager);
    }
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

}
