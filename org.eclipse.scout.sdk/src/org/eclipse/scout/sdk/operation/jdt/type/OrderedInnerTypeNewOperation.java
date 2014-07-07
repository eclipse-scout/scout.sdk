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
import org.eclipse.jdt.core.IType;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.sourcebuilder.type.ITypeSourceBuilder;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeUtility;

/**
 *
 */
public class OrderedInnerTypeNewOperation extends InnerTypeNewOperation {

  private IType m_orderDefinitionType;

  /**
   * @param name
   * @param declaringType
   */
  public OrderedInnerTypeNewOperation(String name, IType declaringType) {
    super(name, declaringType);
  }

  /**
   * @param name
   * @param declaringType
   * @param formatSource
   */
  public OrderedInnerTypeNewOperation(String name, IType declaringType, boolean formatSource) {
    super(name, declaringType);
    setFormatSource(formatSource);
  }

  public OrderedInnerTypeNewOperation(ITypeSourceBuilder sourceBuilder, IType declaringType) {
    super(sourceBuilder, declaringType);
  }

  @Override
  protected void createType(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException {
    Double orderNr = ScoutTypeUtility.getOrderNr(getDeclaringType(), getOrderDefinitionType(), getSibling());
    addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(orderNr));
    super.createType(monitor, workingCopyManager);
  }

  public void setOrderDefinitionType(IType orderDefinitionType) {
    m_orderDefinitionType = orderDefinitionType;
  }

  public IType getOrderDefinitionType() {
    return m_orderDefinitionType;
  }
}
