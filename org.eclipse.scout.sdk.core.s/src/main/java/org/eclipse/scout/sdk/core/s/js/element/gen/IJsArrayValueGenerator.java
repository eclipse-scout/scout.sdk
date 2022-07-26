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

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public interface IJsArrayValueGenerator<VALUE, TYPE extends IJsArrayValueGenerator<VALUE, TYPE>> extends IJsValueGenerator<List<VALUE>, TYPE> {

  default TYPE withValues(List<VALUE> values) {
    return withValue(values);
  }

  @SuppressWarnings("unchecked")
  default TYPE withValues(VALUE... values) {
    return withValues(Arrays.asList(values));
  }

  TYPE withJsValueGeneratorSupplier(Supplier<IJsValueGenerator<VALUE, ?>> createJsValueGenerator);
}
