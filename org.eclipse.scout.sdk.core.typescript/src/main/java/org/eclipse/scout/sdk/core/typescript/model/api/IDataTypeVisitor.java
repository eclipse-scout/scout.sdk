/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.typescript.model.api;

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.util.visitor.IBreadthFirstVisitor;

/**
 * Breadth-first visitor for {@link IDataType}.<br>
 * Steps into {@link IDataType#childTypes()} by default. But the visitor may adjust/extend/modify the child types before
 * visiting.
 */
public interface IDataTypeVisitor extends IBreadthFirstVisitor<IDataType> {

  /**
   * Visitor callback to adjust the child {@link IDataType datatypes} before visiting the next level.
   * 
   * @param parent
   *          The parent {@link IDataType}. Is never {@code null}.
   * @return The adjusted children for the given parent. Must not be {@code null}.
   */
  default Stream<IDataType> childTypes(IDataType parent) {
    if (parent == null) {
      return Stream.empty();
    }
    return parent.childTypes();
  }
}
