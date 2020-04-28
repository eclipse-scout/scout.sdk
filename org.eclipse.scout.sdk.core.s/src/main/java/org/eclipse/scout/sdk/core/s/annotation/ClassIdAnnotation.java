/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.s.annotation;

import java.util.Optional;

import org.eclipse.scout.sdk.core.model.api.AbstractManagedAnnotation;
import org.eclipse.scout.sdk.core.model.api.IAnnotatable;
import org.eclipse.scout.sdk.core.s.IScoutRuntimeTypes;

/**
 * <h3>{@link ClassIdAnnotation}</h3>
 *
 * @since 5.2.0
 */
public class ClassIdAnnotation extends AbstractManagedAnnotation {

  public static final String TYPE_NAME = IScoutRuntimeTypes.ClassId;
  public static final String VALUE_ELEMENT_NAME = "value";

  public static Optional<String> valueOf(IAnnotatable owner) {
    return owner.annotations()
        .withManagedWrapper(ClassIdAnnotation.class)
        .first()
        .map(ClassIdAnnotation::value);
  }

  public String value() {
    return getValue(VALUE_ELEMENT_NAME, String.class, null);
  }
}
