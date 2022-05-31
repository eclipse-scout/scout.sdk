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
