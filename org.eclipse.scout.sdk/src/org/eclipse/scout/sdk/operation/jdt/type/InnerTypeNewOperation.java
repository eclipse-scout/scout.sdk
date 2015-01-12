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
import org.eclipse.scout.sdk.operation.jdt.icu.ImportsCreateOperation;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.sourcebuilder.type.TypeSourceBuilder;
import org.eclipse.scout.sdk.util.resources.ResourceUtility;
import org.eclipse.scout.sdk.util.signature.ImportValidator;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;

/**
 * <h3>{@link InnerTypeNewOperation}</h3>
 *
 * @author Andreas Hoegger
 * @since 3.10.0 08.03.2013
 */
public class InnerTypeNewOperation extends AbstractTypeNewOperation {

  private IJavaElement m_sibling;
  private IType m_declaringType;

  public InnerTypeNewOperation(String typeName, IType declaringType) {
    this(new TypeSourceBuilder(typeName), declaringType);
  }

  public InnerTypeNewOperation(ITypeSourceBuilder sourceBuilder, IType declaringType) {
    super(sourceBuilder);
    sourceBuilder.setParentFullyQualifiedName(declaringType.getFullyQualifiedName('.'));
    m_declaringType = declaringType;
  }

  @Override
  public void validate() {
    if (!TypeUtility.exists(getDeclaringType())) {
      throw new IllegalArgumentException("Declaring type does not exist!");
    }
    super.validate();
  }

  @Override
  public final void run(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    createType(monitor, workingCopyManager);
    formatSource(monitor, workingCopyManager);
  }

  protected void formatSource(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    if (isFormatSource()) {
      JavaElementFormatOperation formatOp = new JavaElementFormatOperation(getCreatedType(), true);
      formatOp.run(monitor, workingCopyManager);
    }
  }

  protected void createType(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    ICompilationUnit icu = getDeclaringType().getCompilationUnit();
    ImportValidator importValidator = new ImportValidator(icu);
    StringBuilder sourceBuilder = new StringBuilder();

    workingCopyManager.register(icu, monitor);
    getSourceBuilder().createSource(sourceBuilder, ResourceUtility.getLineSeparator(icu), getDeclaringType().getJavaProject(), importValidator);
    setCreatedType(getDeclaringType().createType(sourceBuilder.toString(), getSibling(), true, monitor));
    new ImportsCreateOperation(icu, importValidator).run(monitor, workingCopyManager);
  }

  protected void setDeclaringType(IType declaringType) {
    m_declaringType = declaringType;
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
