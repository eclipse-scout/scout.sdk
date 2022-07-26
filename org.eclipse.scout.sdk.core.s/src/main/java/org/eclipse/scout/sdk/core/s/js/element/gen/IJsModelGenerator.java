/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.js.element.gen;

import java.util.function.Predicate;

import org.eclipse.scout.sdk.core.s.js.element.JsElementModel;

public interface IJsModelGenerator<TYPE extends IJsModelGenerator<TYPE>> extends IJsSourceGenerator<IJsSourceBuilder<?>> {

  TYPE withJsModelPropertyGenerator(IJsModelPropertyGenerator<?> jsModelPropertyGenerator);

  TYPE withoutJsModelPropertyGenerator(Predicate<IJsModelPropertyGenerator<?>> removalFilter);

  TYPE withJsElementModel(JsElementModel jsElementModel);
}
