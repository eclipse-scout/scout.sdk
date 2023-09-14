/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.s.model.js.prop;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType.DataTypeFlavor;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataTypeVisitor;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.util.visitor.IBreadthFirstVisitor;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

/**
 * An {@link IDataTypeVisitor} that steps into known @eclipse-scout/core data types (e.g. "ObjectOrChildModel",
 * "ChildModelOf", "ObjectOrModel", "FullModelOf", "ModelOf", "InitModelOf", "StatusOrModel" or "LookupCallOrModel").
 * For such types it directly steps into the targeted original type instead.
 */
public class ScoutJsCoreDataTypesUnwrapVisitor implements IDataTypeVisitor {

  private final IBreadthFirstVisitor<IDataType> m_wrappedVisitor;

  public ScoutJsCoreDataTypesUnwrapVisitor(IBreadthFirstVisitor<IDataType> wrappedVisitor) {
    m_wrappedVisitor = wrappedVisitor;
  }

  @Override
  public TreeVisitResult visit(IDataType element, int level, int index) {
    return unwrap(element)
        .map(l -> m_wrappedVisitor.visit(l, level, index))
        .orElse(TreeVisitResult.SKIP_SUBTREE);
  }

  @Override
  public Stream<IDataType> childTypes(IDataType parent) {
    return unwrappedChildren(parent);
  }

  public static Optional<IDataType> unwrap(IDataType element) {
    if (element == null) {
      return Optional.empty();
    }
    if (element.flavor() == DataTypeFlavor.Array) {
      return element.childTypes().findAny()
          .flatMap(ScoutJsCoreDataTypesUnwrapVisitor::unwrap)
          .map(unwrapped -> unwrapped.createArrayType(element.arrayDimension()));
    }

    if (!(element instanceof IES6Class clazz)) {
      return Optional.of(element);
    }

    var rawClass = clazz.withoutTypeArguments();
    var containingModule = rawClass.containingModule();
    if (!ScoutJsCoreConstants.SCOUT_JS_CORE_MODULE_NAME.equals(containingModule.name())) {
      return Optional.of(element);
    }

    var className = rawClass.name();
    if (ScoutJsCoreConstants.CLASS_NAMES_MODEL_TYPES.contains(className)) {
      return clazz
          .typeArguments()
          .findFirst();
    }
    if (ScoutJsCoreConstants.CLASS_NAME_STATUS_OR_MODEL.equals(className)) {
      return containingModule
          .export(ScoutJsCoreConstants.CLASS_NAME_STATUS)
          .map(s -> (IDataType) s);
    }
    if (ScoutJsCoreConstants.CLASS_NAME_LOOKUP_CALL_OR_MODEL.equals(className)) {
      return containingModule
          .export(ScoutJsCoreConstants.CLASS_NAME_LOOKUP_CALL)
          .map(s -> (IDataType) s);
    }
    return Optional.of(element);
  }

  public static Stream<IDataType> unwrappedChildren(IDataType parent) {
    return parent.childTypes().flatMap(c -> unwrap(c).stream());
  }
}
