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

import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.util.visitor.IBreadthFirstVisitor;

public interface IDataTypeVisitor extends IBreadthFirstVisitor<IDataType> {

  default Stream<IDataType> childTypes(IDataType parent) {
    if (parent == null) {
      return Stream.empty();
    }
    return parent.childTypes();
  }
}
