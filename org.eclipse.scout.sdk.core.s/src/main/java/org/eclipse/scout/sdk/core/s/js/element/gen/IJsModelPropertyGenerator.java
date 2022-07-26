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

import java.util.Optional;

public interface IJsModelPropertyGenerator<TYPE extends IJsModelPropertyGenerator<TYPE>> extends IJsSourceGenerator<IJsSourceBuilder<?>> {

  String ID_PROPERTY = "id";
  String OBJECT_TYPE_PROPERTY = "objectType";

  Optional<String> identifier();

  TYPE withIdentifier(String identifier);

  TYPE withJsValueGenerator(IJsValueGenerator<?, ?> jsValueGenerator);
}
