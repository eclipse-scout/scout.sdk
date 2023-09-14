/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api.internal;

import org.eclipse.scout.sdk.core.typescript.model.api.AbstractNodeElement;
import org.eclipse.scout.sdk.core.typescript.model.api.DataTypeAssignableEvaluator;
import org.eclipse.scout.sdk.core.typescript.model.api.DataTypeNameEvaluator;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataTypeVisitor;
import org.eclipse.scout.sdk.core.typescript.model.spi.DataTypeSpi;
import org.eclipse.scout.sdk.core.util.visitor.IBreadthFirstVisitor;
import org.eclipse.scout.sdk.core.util.visitor.TreeTraversals;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

public class DataTypeImplementor<SPI extends DataTypeSpi> extends AbstractNodeElement<SPI> implements IDataType {

  public DataTypeImplementor(SPI spi) {
    super(spi);
  }

  @Override
  public String name() {
    return spi().name();
  }

  @Override
  public boolean isAssignableFrom(IDataType childType) {
    return new DataTypeAssignableEvaluator(childType).fulfills(this);
  }

  @Override
  public TreeVisitResult visit(IDataTypeVisitor visitor) {
    IBreadthFirstVisitor<DataTypeSpi> spiVisitor = (element, level, index) -> visitor.visit(element.api(), level, index);
    return TreeTraversals.create(spiVisitor, d -> visitor.childTypes(d.api()).map(IDataType::spi)).traverse(spi());
  }

  @Override
  public boolean isPrimitive() {
    return spi().isPrimitive();
  }

  @Override
  public String toString() {
    return new DataTypeNameEvaluator().eval(this) + " [" + containingModule() + ']';
  }
}
