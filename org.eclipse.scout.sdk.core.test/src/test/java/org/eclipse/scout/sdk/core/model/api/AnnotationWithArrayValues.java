/*
 * Copyright (c) 2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.sdk.core.model.api;

/**
 * <h3>{@link AnnotationWithArrayValues}</h3>
 *
 * @since 6.1.0
 */
public class AnnotationWithArrayValues extends AbstractManagedAnnotation {
  public static final String TYPE_NAME = org.eclipse.scout.sdk.core.fixture.AnnotationWithArrayValues.class.getName();

  public AnnotationWithSingleValues[] annos() {
    return getValue("annos", AnnotationWithSingleValues[].class, null);
  }
}
