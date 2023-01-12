/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.sdk.core.apidef;

import java.util.Optional;
import java.util.function.Function;

/**
 * Represents a generic {@link Function} that takes a {@link Class}<{@code T}> and returns an
 * {@link Optional}<{@code T}> where {@code T extends }{@link IApiSpecification}.
 */
@FunctionalInterface
public interface OptApiFunction {
  <T extends IApiSpecification> Optional<T> apply(Class<T> c);
}
