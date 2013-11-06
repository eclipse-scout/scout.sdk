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
import org.eclipse.scout.sdk.operation.jdt.annotation.OrderAnnotationsUpdateOperation;
import org.eclipse.scout.sdk.sourcebuilder.annotation.AnnotationSourceBuilderFactory;
import org.eclipse.scout.sdk.util.type.TypeFilters;
import org.eclipse.scout.sdk.util.type.TypeUtility;
import org.eclipse.scout.sdk.util.typecache.ITypeHierarchy;
import org.eclipse.scout.sdk.util.typecache.IWorkingCopyManager;
import org.eclipse.scout.sdk.workspace.type.ScoutTypeComparators;

/**
 *
 */
public class OrderedInnerTypeNewOperation extends InnerTypeNewOperation {

  private IType m_orderDefinitionType;
  private double m_orderNr = -1.0;

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

  @Override
  protected void createType(IProgressMonitor monitor, IWorkingCopyManager workingCopyManager) throws CoreException, IllegalArgumentException {
    if (getOrderNr() < 0) {
      updateOrderNumbers(monitor, workingCopyManager);
    }
    addAnnotationSourceBuilder(AnnotationSourceBuilderFactory.createOrderAnnotation(m_orderNr));
    super.createType(monitor, workingCopyManager);
  }

  protected void updateOrderNumbers(IProgressMonitor monitor, IWorkingCopyManager manager) throws IllegalArgumentException, CoreException {
    m_orderNr = -1.0;
    if (getOrderDefinitionType() != null) {
      ITypeHierarchy typeHierarchy = TypeUtility.getLocalTypeHierarchy(getDeclaringType());
      IType[] innerTypes = TypeUtility.getInnerTypes(getDeclaringType(), TypeFilters.getSubtypeFilter(getOrderDefinitionType(), typeHierarchy), ScoutTypeComparators.getOrderAnnotationComparator());
      OrderAnnotationsUpdateOperation orderAnnotationOp = new OrderAnnotationsUpdateOperation(getDeclaringType());
      double tempOrderNr = 10.0;
      for (IType innerType : innerTypes) {
        if (innerType.equals(getSibling())) {
          m_orderNr = tempOrderNr;
          tempOrderNr += 10.0;
        }
        orderAnnotationOp.addOrderAnnotation(innerType, tempOrderNr);
        tempOrderNr += 10.0;
      }
      if (m_orderNr < 0) {
        m_orderNr = tempOrderNr;
      }
      orderAnnotationOp.validate();
      orderAnnotationOp.run(monitor, manager);
      manager.reconcile(getDeclaringType().getCompilationUnit(), monitor);
    }
  }

  public void setOrderDefinitionType(IType orderDefinitionType) {
    m_orderDefinitionType = orderDefinitionType;
  }

  public IType getOrderDefinitionType() {
    return m_orderDefinitionType;
  }

  public void setOrderNr(double orderNr) {
    m_orderNr = orderNr;
  }

  public double getOrderNr() {
    return m_orderNr;
  }

}
