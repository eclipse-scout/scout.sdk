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

import org.eclipse.scout.sdk.core.s.model.js.ScoutJsCoreConstants;
import org.eclipse.scout.sdk.core.s.model.js.ScoutJsModels;
import org.eclipse.scout.sdk.core.s.model.js.objects.IScoutJsObject;
import org.eclipse.scout.sdk.core.typescript.model.api.IDataType;
import org.eclipse.scout.sdk.core.typescript.model.api.IES6Class;
import org.eclipse.scout.sdk.core.util.visitor.IBreadthFirstVisitor;
import org.eclipse.scout.sdk.core.util.visitor.TreeVisitResult;

public class ScoutJsCoreDataTypesUnwrapVisitor implements IBreadthFirstVisitor<IDataType> {

  private final IBreadthFirstVisitor<IDataType> m_wrappedVisitor;

  public ScoutJsCoreDataTypesUnwrapVisitor(IBreadthFirstVisitor<IDataType> wrappedVisitor) {
    m_wrappedVisitor = wrappedVisitor;
  }

  @Override
  public TreeVisitResult visit(IDataType element, int level, int index) {
    var containingModule = element.containingModule();
    if (element instanceof IES6Class clazz && ScoutJsCoreConstants.SCOUT_JS_CORE_MODULE_NAME.equals(containingModule.name())) {
      var className = clazz.name();
      var scoutJsCoreModel = ScoutJsModels.create(containingModule).orElseThrow();
      if (ScoutJsCoreConstants.CLASS_NAMES_MODEL_TYPES.contains(className)) {
        return clazz
            .typeArguments()
            .findFirst()
            .map(arg -> m_wrappedVisitor.visit(arg, level, index))
            .orElse(TreeVisitResult.SKIP_SUBTREE);
      }
      if (ScoutJsCoreConstants.CLASS_NAME_STATUS_OR_MODEL.equals(className)) {
        var status = scoutJsCoreModel.exportedScoutObjects().get(ScoutJsCoreConstants.CLASS_NAME_STATUS);
        return Optional.ofNullable(status)
            .map(IScoutJsObject::declaringClass)
            .map(s -> m_wrappedVisitor.visit(s, level, index))
            .orElse(TreeVisitResult.SKIP_SUBTREE);
      }
      if (ScoutJsCoreConstants.CLASS_NAME_LOOKUP_CALL_OR_MODEL.equals(className)) {
        var lookupCall = scoutJsCoreModel.exportedScoutObjects().get(ScoutJsCoreConstants.CLASS_NAME_LOOKUP_CALL);
        return Optional.ofNullable(lookupCall)
            .map(IScoutJsObject::declaringClass)
            .map(l -> m_wrappedVisitor.visit(l, level, index))
            .orElse(TreeVisitResult.SKIP_SUBTREE);
      }
    }
    return m_wrappedVisitor.visit(element, level, index);
  }
}
